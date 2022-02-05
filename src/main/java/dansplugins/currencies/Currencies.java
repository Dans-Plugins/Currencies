package dansplugins.currencies;

import dansplugins.currencies.bstats.Metrics;
import dansplugins.currencies.services.LocalConfigService;
import dansplugins.currencies.services.LocalCommandService;
import dansplugins.currencies.services.LocalStorageService;
import dansplugins.currencies.utils.EventRegistry;
import dansplugins.currencies.utils.Scheduler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Currencies extends JavaPlugin {

    private static Currencies instance;

    private final String pluginVersion = "v" + getDescription().getVersion();

    public static Currencies getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        initializeConfig();
        EventRegistry.getInstance().registerEvents();
        LocalStorageService.getInstance().load();
        handlebStatsIntegration();
        Scheduler.getInstance().scheduleAutosave();
    }

    @Override
    public void onDisable() {
        LocalStorageService.getInstance().save();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        LocalCommandService localCommandService = new LocalCommandService();
        return localCommandService.interpretCommand(sender, label, args);
    }

    public String getVersion() {
        return pluginVersion;
    }

    public boolean isDebugEnabled() {
        return LocalConfigService.getInstance().getBoolean("debugMode");
    }

    private boolean isVersionMismatched() {
        return !getConfig().getString("version").equalsIgnoreCase(getVersion());
    }

    private void initializeConfig() {
        if (!(new File("./plugins/Currencies/config.yml").exists())) {
            LocalConfigService.getInstance().saveMissingConfigDefaultsIfNotPresent();
        }
        else {
            // pre load compatibility checks
            if (isVersionMismatched()) {
                LocalConfigService.getInstance().saveMissingConfigDefaultsIfNotPresent();
            }
            reloadConfig();
        }
    }

    private void handlebStatsIntegration() {
        int pluginId = 12810;
        Metrics metrics = new Metrics(this, pluginId);
    }
}