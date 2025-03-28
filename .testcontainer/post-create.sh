#!/bin/bash

SERVER_DIR="/testmcserver"
BUILD_DIR="/testmcserver-build"
RESOURCES_DIR="/resources/jars"

# Function: Log a message with the [POST-CREATE] prefix
log() {
    local message="$1"
    echo "[POST-CREATE] $message"
}

# Function: Setup server
setup_server() {
    if [ -z "$(ls -A "$SERVER_DIR")" ] || [ "$OVERWRITE_EXISTING_SERVER" = "true" ]; then
        rm -rf "$SERVER_DIR"/*
        log "Copying "spigot-${MINECRAFT_VERSION}.jar" to server directory..."
        cp "$BUILD_DIR"/spigot-"${MINECRAFT_VERSION}".jar "$SERVER_DIR"/spigot-"${MINECRAFT_VERSION}".jar
        mkdir "$SERVER_DIR"/plugins
    else
        log "Server is already set up."
    fi
}

# Function: Setup ops.json file
setup_ops_file() {
    log "Creating ops.json file..."
    cat <<EOF > /testmcserver/ops.json
    [
      {
        "uuid": "${OPERATOR_UUID}",
        "name": "${OPERATOR_NAME}",
        "level": ${OPERATOR_LEVEL},
        "bypassesPlayerLimit": false
      }
    ]
EOF
}

# Function: Accept EULA
accept_eula() {
    log "Accepting Minecraft EULA..."
    echo "eula=true" > "$SERVER_DIR"/eula.txt
}

# Function: Copy the latest plugin JAR with timestamp check
copy_latest_plugin_jar() {
    log "Copying the latest plugin JAR..."
    local jarFile=$(find "$BUILD_DIR/Currencies/build/libs" -name "*-all.jar" -type f -print -quit)

    if [ -z "$jarFile" ]; then
        log "ERROR: No plugin JAR file found in the build directory."
        return 1
    fi

    local currentDate=$(date +%s)
    local jarDate=$(stat -c %Y "$jarFile")
    local diff=$((currentDate - jarDate))

    if [ $diff -gt 300 ]; then
        log "WARNING: The plugin JAR is older than 5 minutes. It may be necessary to rebuild the plugin."
    fi

    cp "$jarFile" "$SERVER_DIR/plugins" || log "ERROR: Failed to copy the plugin JAR."
}

# Function: Copy the MedievalFactions plugin JAR
copy_medieval_factions_plugin_jar() {
    log "Copying the MedievalFactions plugin JAR..."
    cp "$RESOURCES_DIR"/medieval-factions-*.jar "$SERVER_DIR"/plugins
}

# Function: Start server
start_server() {
    log "Starting server..."
    java -jar "$SERVER_DIR"/spigot-"${MINECRAFT_VERSION}".jar
}

# Main Process
log "Running 'post-create.sh' script..."
setup_server
setup_ops_file
accept_eula
if ! copy_latest_plugin_jar; then
    log "Exiting script due to error in copying the latest plugin JAR."
    exit 1
fi
copy_medieval_factions_plugin_jar

# Start Server
start_server