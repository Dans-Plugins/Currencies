package dansplugins.currencies.services;

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

/**
 * @author Daniel McCoy Stephenson
 */
public class ConfigService {
    private final Currencies currencies;

    private boolean altered = false;

    public ConfigService(Currencies currencies) {
        this.currencies = currencies;
    }

    public void saveMissingConfigDefaultsIfNotPresent() {
        // set version
        if (!getConfig().isString("version")) {
            getConfig().addDefault("version", currencies.getVersion());
        }
        else {
            getConfig().set("version", currencies.getVersion());
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
        if (!getConfig().isSet("disallowSmelting")){
            getConfig().set("disallowSmelting", true);
        }
        if (!getConfig().isSet("disallowPlacement")) {
            getConfig().set("disallowPlacement", true);
        }
        if (!getConfig().isSet("showAmountMinted")) {
            getConfig().set("showAmountMinted", true);
        }
        if (!getConfig().isSet("disallowAnvilUsage")) {
            getConfig().set("disallowAnvilUsage", true);
        }
        if (!getConfig().isSet("itemCost")) {
            getConfig().set("itemCost", true);
        }
        getConfig().options().copyDefaults(true);
        currencies.saveConfig();
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
                    || option.equalsIgnoreCase("disallowCrafting")
                    || option.equalsIgnoreCase("disallowPlacement")
                    || option.equalsIgnoreCase("showAmountMinted")
                    || option.equalsIgnoreCase("disallowAnvilUsage")
                    || option.equalsIgnoreCase("itemCost")) {
                getConfig().set(option, Boolean.parseBoolean(value));
                sender.sendMessage(ChatColor.GREEN + "Boolean set.");
            } else if (option.equalsIgnoreCase("powerCost")) {
                getConfig().set(option, Double.parseDouble(value));
                sender.sendMessage(ChatColor.GREEN + "Double set.");
            } else if (option.equalsIgnoreCase("disallowSmelting")) {
                getConfig().set(option, Boolean.parseBoolean(value));
                sender.sendMessage(ChatColor.GREEN + "Boolean set.");
            } else {
                getConfig().set(option, value);
                sender.sendMessage(ChatColor.GREEN + "String set.");
            }

            // save
            currencies.saveConfig();
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
                + ", disallowCrafting: " + getBoolean("disallowCrafting")
                + ", disallowPlacement: " + getBoolean("disallowPlacement")
                + ", showAmountMinted: " + getBoolean("showAmountMinted")
                + ", disallowAnvilUsage: " + getBoolean("disallowAnvilUsage")
                + ", disallowSmelting: " + getBoolean("disallowSmelting")
                + ", itemCost: " + getBoolean("itemCost"));
    }

    public boolean hasBeenAltered() {
        return altered;
    }

    public FileConfiguration getConfig() {
        return currencies.getConfig();
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