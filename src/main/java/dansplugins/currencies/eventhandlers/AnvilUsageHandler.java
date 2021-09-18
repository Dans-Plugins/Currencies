package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.Currencies;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilUsageHandler implements Listener {

    @EventHandler()
    public void handle(InventoryClickEvent event) {

        if (event.getInventory() instanceof AnvilInventory) {
            if (Currencies.getInstance().isDebugEnabled()) {
                System.out.println("[DEBUG] An anvil inventory is being interacted with.");
                for (ItemStack itemStack : event.getInventory()) {
                    if (itemStack != null) {
                        ItemMeta meta = itemStack.getItemMeta();
                        if (meta != null) {
                            System.out.println("[ICE] " + itemStack.getItemMeta().getDisplayName());
                        }
                    }
                }
            }
        }


    }

    @EventHandler()
    public void handle(PrepareAnvilEvent event) {
        if (Currencies.getInstance().isDebugEnabled()) {
            System.out.println("[DEBUG] Prepare Anvil Event is firing.");
            printContents(event.getInventory());
            Player player = (Player) event.getViewers().get(0);
            player.sendMessage(ChatColor.RED + "Cancelled.");
            event.setResult(null);
        }
    }

    private void printContents(Inventory inventory) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    System.out.println("[PAE] " + itemStack.getItemMeta().getDisplayName());
                }
            }
        }
    }

}
