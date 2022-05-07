package dansplugins.currencies;

import dansplugins.currencies.bstats.Metrics;
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
import dansplugins.currencies.eventhandlers.AnvilUsageHandler;
import dansplugins.currencies.eventhandlers.CraftingHandler;
import dansplugins.currencies.eventhandlers.FactionEventHandler;
import dansplugins.currencies.eventhandlers.FurnaceUsageHandler;
import dansplugins.currencies.eventhandlers.InteractionHandler;
import dansplugins.currencies.eventhandlers.JoinHandler;
import dansplugins.currencies.eventhandlers.PlacementHandler;
import dansplugins.currencies.services.LocalConfigService;
import dansplugins.currencies.services.LocalStorageService;
import dansplugins.currencies.utils.Logger;
import dansplugins.currencies.utils.Scheduler;
import dansplugins.factionsystem.MedievalFactions;
import dansplugins.factionsystem.externalapi.MedievalFactionsAPI;
import preponderous.ponder.minecraft.bukkit.PonderMC;
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
    private static Currencies instance;
    private final String pluginVersion = "v" + getDescription().getVersion();
    private MedievalFactionsAPI medievalFactionsAPI;
    private CommandService commandService = new CommandService((PonderMC) getPonder());

    /**
     * This can be used to get the instance of the main class that is managed by itself.
     * @return The managed instance of the main class.
     */
    public static Currencies getInstance() {
        return instance;
    }

    /**
     * This runs when the server starts.
     */
    @Override
    public void onEnable() {
        instance = this;
        initializeMFAPI();
        initializeConfig();
        registerEventHandlers();
        initializeCommandService();
        LocalStorageService.getInstance().load();
        Scheduler.getInstance().scheduleAutosave();
        handlebStatsIntegration();
    }

    private void initializeMFAPI() {
        try {
            this.medievalFactionsAPI = MedievalFactions.getInstance().getAPI();
        } catch(Exception e) {
            this.medievalFactionsAPI = null;
            Logger.getInstance().log("Something went wrong initializing the MF API.");
        }
    }

    /**
     * This runs when the server stops.
     */
    @Override
    public void onDisable() {
        LocalStorageService.getInstance().save();
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
            DefaultCommand defaultCommand = new DefaultCommand();
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
        return LocalConfigService.getInstance().getBoolean("debugMode");
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
            LocalConfigService.getInstance().saveMissingConfigDefaultsIfNotPresent();
        }
    }

    private boolean configFileExists() {
        return new File("./plugins/" + getName() + "/config.yml").exists();
    }

    private void performCompatibilityChecks() {
        if (isVersionMismatched()) {
            LocalConfigService.getInstance().saveMissingConfigDefaultsIfNotPresent();
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
                new AnvilUsageHandler(),
                new CraftingHandler(),
                new FactionEventHandler(),
                new FurnaceUsageHandler(),
                new InteractionHandler(),
                new JoinHandler(),
                new PlacementHandler()
        ));
        eventHandlerRegistry.registerEventHandlers(listeners, this);
    }

        /**
     * Initializes Ponder's command service with the plugin's commands.
     */
    private void initializeCommandService() {
        ArrayList<AbstractPluginCommand> commands = new ArrayList<>(Arrays.asList(
                new HelpCommand(),
                new BalanceCommand(),
                new CreateCommand(),
                new DepositCommand(),
                new DescCommand(),
                new ForceCommand(),
                new InfoCommand(),
                new ListCommand(),
                new MintCommand(),
                new RenameCommand(),
                new RetireCommand(),
                new WithdrawCommand(),
                new ConfigCommand()
        ));
        commandService.initialize(commands, "That command wasn't found.");
    }
}