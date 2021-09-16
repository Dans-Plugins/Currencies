package dansplugins.currencies.managers;

public class ConfigManager {

    private static ConfigManager instance;

    private ConfigManager() {

    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    // TODO: implement rest of the methods

    public boolean hasBeenAltered() {
        // TODO: implement this method
        return false;
    }
}
