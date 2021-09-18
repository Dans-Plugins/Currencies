package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.managers.CurrencyManager;
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

import java.util.List;

public class AnvilUsageHandler implements Listener {

    @EventHandler()
    public void handle(PrepareAnvilEvent event) {
        if (Currencies.getInstance().isDebugEnabled()) {
            System.out.println("[DEBUG] Prepare Anvil Event is firing.");
        }

        Player player = (Player) event.getViewers().get(0);

        if (player == null) {
            return;
        }

        Inventory inventory = event.getInventory();
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) {
                continue;
            }
            if (CurrencyManager.getInstance().isCurrency(itemStack)) {
                player.sendMessage(ChatColor.RED + "You can't use currencies when renaming or repairing.");
                event.setResult(null);
                return;
            }
        }
    }

    private void printContents(Inventory inventory) {
        for (ItemStack itemStack : inventory) {
            if (itemStack != null) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore();
                    if (lore != null) {
                        System.out.println("[ICE] " + lore);
                    }
                }
            }
        }
    }

}
