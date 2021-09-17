package dansplugins.currencies.managers;

import dansplugins.currencies.Currencies;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

/*
    To add a new config option, the following methods must be altered:
    - saveMissingConfigDefaultsIfNotPresent
    - setConfigOption()
    - sendConfigList()
 */

public class ConfigManager {

    private static ConfigManager instance;
    private boolean altered = false;

    private ConfigManager() {

    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void saveMissingConfigDefaultsIfNotPresent() {
        // set version
        if (!getConfig().isString("version")) {
            getConfig().addDefault("version", Currencies.getInstance().getVersion());
        }
        else {
            getConfig().set("version", Currencies.getInstance().getVersion());
        }

        // save config options
        if (!getConfig().isSet("debugMode")) {
            getConfig().set("debugMode", false);
        }
        if (!getConfig().isSet("maxCurrencyIDNumber")) {
            getConfig().set("maxCurrencyIDNumber", 1000000);
        }
        if (!getConfig().isSet("powerCostEnabled")) {
            getConfig().set("powerCostEnabled", true);
        }
        if (!getConfig().isSet("powerCost")) {
            getConfig().set("powerCost", 0.5);
        }
        if (!getConfig().isSet("minimumPowerCost")) {
            getConfig().set("minimumPowerCost", 1);
        }
        if (!getConfig().isSet("disallowCrafting")) {
            getConfig().set("disallowCrafting", true);
        }
        getConfig().options().copyDefaults(true);
        Currencies.getInstance().saveConfig();
    }

    public void setConfigOption(String option, String value, CommandSender sender) {

        if (getConfig().isSet(option)) {

            if (option.equalsIgnoreCase("version")) {
                sender.sendMessage(ChatColor.RED + "Cannot set version.");
                return;
            } else if (option.equalsIgnoreCase("maxCurrencyIDNumber")
                    || option.equalsIgnoreCase("minimumPowerCost")) {
                getConfig().set(option, Integer.parseInt(value));
                sender.sendMessage(ChatColor.GREEN + "Integer set.");
            } else if (option.equalsIgnoreCase("debugMode")
                    || option.equalsIgnoreCase("powerCostEnabled")
                    || option.equalsIgnoreCase("disallowCrafting")) {
                getConfig().set(option, Boolean.parseBoolean(value));
                sender.sendMessage(ChatColor.GREEN + "Boolean set.");
            } else if (option.equalsIgnoreCase("powerCost")) {
                getConfig().set(option, Double.parseDouble(value));
                sender.sendMessage(ChatColor.GREEN + "Double set.");
            } else {
                getConfig().set(option, value);
                sender.sendMessage(ChatColor.GREEN + "String set.");
            }

            // save
            Currencies.getInstance().saveConfig();
            altered = true;
        } else {
            sender.sendMessage(ChatColor.RED + "That config option wasn't found.");
        }
    }

    public void sendConfigList(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== Config List ===");
        sender.sendMessage(ChatColor.AQUA + "version: " + getConfig().getString("version")
                + ", debugMode: " + getString("debugMode")
                + ", maxCurrencyIDNumber: " + getInt("maxCurrencyIDNumber")
                + ", powerCostEnabled: " + getBoolean("powerCostEnabled")
                + ", powerCost: " + getDouble("powerCost")
                + ", disallowCrafting: " + getBoolean("disallowCrafting"));
    }

    public boolean hasBeenAltered() {
        return altered;
    }

    public FileConfiguration getConfig() {
        return Currencies.getInstance().getConfig();
    }

    public int getInt(String option) {
        return getConfig().getInt(option);
    }

    public boolean getBoolean(String option) {
        return getConfig().getBoolean(option);
    }

    public double getDouble(String option) {
        return getConfig().getDouble(option);
    }

    public String getString(String option) {
        return getConfig().getString(option);
    }

}