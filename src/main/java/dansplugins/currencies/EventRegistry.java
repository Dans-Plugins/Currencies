package dansplugins.currencies;
import dansplugins.currencies.eventhandlers.*;
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
        manager.registerEvents(new InteractionHandler(), mainInstance);
    }
}
