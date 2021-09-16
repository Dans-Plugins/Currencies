package dansplugins.currencies;

import dansplugins.currencies.managers.StorageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Currencies extends JavaPlugin {

    private static Currencies instance;

    private final String version = "v0.5";

    public static Currencies getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        StorageManager.getInstance().load();

        EventRegistry.getInstance().registerEvents();
    }

    @Override
    public void onDisable() {
        StorageManager.getInstance().save();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        CommandInterpreter commandInterpreter = new CommandInterpreter();
        return commandInterpreter.interpretCommand(sender, label, args);
    }

    public String getVersion() {
        return version;
    }

    public boolean isDebugEnabled() {
        // return ConfigManager.getInstance().getBoolean("debugMode");
        return true;
    }

    private boolean isVersionMismatched() {
        return !getConfig().getString("version").equalsIgnoreCase(getVersion());
    }
}