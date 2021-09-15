package dansplugins.currencies.utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

import static org.bukkit.Bukkit.getOfflinePlayers;
import static org.bukkit.Bukkit.getOnlinePlayers;

public class UUIDChecker {

    private static dansplugins.factionsystem.utils.UUIDChecker instance;

    public static dansplugins.factionsystem.utils.UUIDChecker getInstance() {
        if (instance == null) instance = new dansplugins.factionsystem.utils.UUIDChecker();
        return instance;
    }

    public String findPlayerNameBasedOnUUID(UUID playerUUID) {
        // Check online
        for (Player player : getOnlinePlayers()) {
            if (player.getUniqueId().equals(playerUUID)) {
                return player.getName();
            }
        }
        // Check offline
        for (OfflinePlayer player : getOfflinePlayers()) {
            if (player.getUniqueId().equals(playerUUID)) {
                return player.getName();
            }
        }
        return "";
    }

    public UUID findUUIDBasedOnPlayerName(String playerName) {
        // Check online
        for (Player player : getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(playerName)) {
                return player.getUniqueId();
            }
        }
        // Check offline
        for (OfflinePlayer player : getOfflinePlayers()) {
            try {
                if (player.getName().equalsIgnoreCase(playerName)) {
                    return player.getUniqueId();
                }
            } catch (NullPointerException e) {
                // Fail silently as quit possibly common.
            }
        }
        return null;
    }
}
