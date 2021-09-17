package dansplugins.currencies;
import dansplugins.currencies.eventhandlers.CraftingHandler;
import dansplugins.currencies.eventhandlers.FactionEventHandler;
import dansplugins.currencies.eventhandlers.JoinHandler;
import dansplugins.currencies.eventhandlers.PlacementHandler;
import org.bukkit.plugin.PluginManager;

public class EventRegistry {

    private static EventRegistry instance;

    private EventRegistry() {

    }

    public static EventRegistry getInstance() {
        if (instance == null) {
            instance = new EventRegistry();
        }
        return instance;
    }

    public void registerEvents() {

        Currencies mainInstance = Currencies.getInstance();
        PluginManager manager = mainInstance.getServer().getPluginManager();

        // blocks and interaction
        manager.registerEvents(new JoinHandler(), mainInstance);
        manager.registerEvents(new CraftingHandler(), mainInstance);
        manager.registerEvents(new PlacementHandler(), mainInstance);
        manager.registerEvents(new FactionEventHandler(), mainInstance);
    }
}
