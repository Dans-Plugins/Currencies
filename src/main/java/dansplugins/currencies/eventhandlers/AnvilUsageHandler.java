package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.Currencies;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;

public class AnvilUsageHandler implements Listener {

    @EventHandler()
    public void handle(InventoryClickEvent event) {

        if (event.getInventory() instanceof AnvilInventory) {
            if (Currencies.getInstance().isDebugEnabled()) { System.out.println("[DEBUG] An anvil inventory is being interacted with."); }
        }

    }

}
