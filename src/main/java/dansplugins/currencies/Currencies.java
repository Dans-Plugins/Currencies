package dansplugins.currencies;

import dansplugins.currencies.commands.BalanceCommand;
import dansplugins.currencies.commands.ConfigCommand;
import dansplugins.currencies.commands.CreateCommand;
import dansplugins.currencies.commands.DefaultCommand;
import dansplugins.currencies.commands.DepositCommand;
import dansplugins.currencies.commands.DescCommand;
import dansplugins.currencies.commands.ForceCommand;
import dansplugins.currencies.commands.HelpCommand;
import dansplugins.currencies.commands.InfoCommand;
import dansplugins.currencies.commands.ListCommand;
import dansplugins.currencies.commands.MintCommand;
import dansplugins.currencies.commands.RenameCommand;
import dansplugins.currencies.commands.RetireCommand;
import dansplugins.currencies.commands.WithdrawCommand;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.factories.CurrencyFactory;
import dansplugins.currencies.listeners.AnvilUsageListener;
import dansplugins.currencies.listeners.CraftingListener;
import dansplugins.currencies.listeners.FactionEventListener;
import dansplugins.currencies.listeners.FurnaceUsageListener;
import dansplugins.currencies.listeners.InteractionListener;
import dansplugins.currencies.listeners.JoinListener;
import dansplugins.currencies.listeners.PlacementListener;
import dansplugins.currencies.services.ConfigService;
import dansplugins.currencies.services.CurrencyService;
import dansplugins.currencies.services.StorageService;
import dansplugins.currencies.utils.Logger;
import dansplugins.currencies.utils.Messenger;
import dansplugins.currencies.utils.Scheduler;
import dansplugins.factionsystem.externalapi.MedievalFactionsAPI;
import dansplugins.factionsystem.shadow.org.bstats.bukkit.Metrics;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.minecraft.bukkit.abs.PonderBukkitPlugin;
import preponderous.ponder.minecraft.bukkit.services.CommandService;
import preponderous.ponder.minecraft.bukkit.tools.EventHandlerRegistry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Daniel McCoy Stephenson
 */
public final class Currencies extends PonderBukkitPlugin {
    private final String pluginVersion = "v" + getDescription().getVersion();
    private MedievalFactionsAPI medievalFactionsAPI;

    private final CommandService commandService = new CommandService(getPonder());
    private final ConfigService configService = new ConfigService(this);
    private final PersistentData persistentData = new PersistentData();
    private final Messenger messenger = new Messenger();
    private final CurrencyService currencyService = new CurrencyService(persistentData, this, messenger, configService);
    private final StorageService storageService = new StorageService(configService, this, persistentData);
    private final Logger logger = new Logger(this);
    private final Scheduler scheduler = new Scheduler(logger, this, storageService);
    private final CurrencyFactory currencyFactory = new CurrencyFactory();


    /**
     * This runs when the server starts.
     */
    @Override
    public void onEnable() {
        initializeMFAPI();
        initializeConfig();
        registerEventHandlers();
        initializeCommandService();
        storageService.load();
        scheduler.scheduleAutosave();
        handlebStatsIntegration();
    }

    private void initializeMFAPI() {
        try {
            this.medievalFactionsAPI = new MedievalFactionsAPI();
        } catch(Exception e) {
            this.medievalFactionsAPI = null;
            logger.log("Something went wrong initializing the MF API.");
        }
    }

    /**
     * This runs when the server stops.
     */
    @Override
    public void onDisable() {
        storageService.save();
    }

    /**
     * This method handles commands sent to the minecraft server and interprets them if the label matches one of the core commands.
     * @param sender The sender of the command.
     * @param cmd The command that was sent. This is unused.
     * @param label The core command that has been invoked.
     * @param args Arguments of the core command. Often sub-commands.
     * @return A boolean indicating whether the execution of the command was successful.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            DefaultCommand defaultCommand = new DefaultCommand(this);
            return defaultCommand.execute(sender);
        }

        return commandService.interpretAndExecuteCommand(sender, label, args);
    }

    /**
     * This can be used to get the version of the plugin.
     * @return A string containing the version preceded by 'v'
     */
    public String getVersion() {
        return pluginVersion;
    }

    /**
     * Checks if debug is enabled.
     * @return Whether debug is enabled.
     */
    public boolean isDebugEnabled() {
        return configService.getBoolean("debugMode");
    }

    public MedievalFactionsAPI getMedievalFactionsAPI() {
        if (medievalFactionsAPI == null) {
           initializeMFAPI();
        }
        return medievalFactionsAPI;
    }

    /**
     * Checks if the version is mismatched.
     * @return A boolean indicating if the version is mismatched.
     */
    public boolean isVersionMismatched() {
        String configVersion = this.getConfig().getString("version");
        if (configVersion == null || this.getVersion() == null) {
            return false;
        } else {
            return !configVersion.equalsIgnoreCase(this.getVersion());
        }
    }

    private void initializeConfig() {
        if (configFileExists()) {
            performCompatibilityChecks();
        }
        else {
            configService.saveMissingConfigDefaultsIfNotPresent();
        }
    }

    private boolean configFileExists() {
        return new File("./plugins/" + getName() + "/config.yml").exists();
    }

    private void performCompatibilityChecks() {
        if (isVersionMismatched()) {
            configService.saveMissingConfigDefaultsIfNotPresent();
        }
        reloadConfig();
    }

    private void handlebStatsIntegration() {
        int pluginId = 12810;
        new Metrics(this, pluginId);
    }

        /**
     * Registers the event handlers of the plugin using Ponder.
     */
    private void registerEventHandlers() {
        EventHandlerRegistry eventHandlerRegistry = new EventHandlerRegistry();
        ArrayList<Listener> listeners = new ArrayList<>(Arrays.asList(
                new AnvilUsageListener(configService, currencyService, this),
                new CraftingListener(configService, logger, currencyService),
                new FactionEventListener(persistentData, logger, currencyService),
                new FurnaceUsageListener(configService, logger, currencyService),
                new InteractionListener(currencyService, persistentData, logger, currencyFactory),
                new JoinListener(persistentData, this),
                new PlacementListener(configService, logger, currencyService)
        ));
        eventHandlerRegistry.registerEventHandlers(listeners, this);
    }

        /**
     * Initializes Ponder's command service with the plugin's commands.
     */
    private void initializeCommandService() {
        ArrayList<AbstractPluginCommand> commands = new ArrayList<>(Arrays.asList(
                new HelpCommand(),
                new BalanceCommand(persistentData),
                new CreateCommand(this, persistentData, currencyService),
                new DepositCommand(persistentData, currencyFactory),
                new DescCommand(this, persistentData),
                new ForceCommand(persistentData, currencyService),
                new InfoCommand(this, persistentData, configService),
                new ListCommand(persistentData, logger),
                new MintCommand(this, persistentData, configService, currencyFactory),
                new RenameCommand(this, persistentData),
                new RetireCommand(this, persistentData, currencyService),
                new WithdrawCommand(persistentData, currencyFactory),
                new ConfigCommand(configService)
        ));
        commandService.initialize(commands, "That command wasn't found.");
    }
}