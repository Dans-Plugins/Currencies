package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.services.LocalConfigService;
import dansplugins.currencies.services.LocalCurrencyService;
import dansplugins.currencies.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class FurnaceUsageHandler implements Listener {

    @EventHandler()
    public void handle(InventoryClickEvent event) {

        if (!LocalConfigService.getInstance().getBoolean("disallowSmelting")) {
            Logger.getInstance().log("[DEBUG] Smelting with currencies is allowed.");
            return;
        }

        for (ItemStack itemStack : event.getInventory()) {
            if (itemStack == null) {
                continue;
            }

            if (event.getSlotType() != InventoryType.SlotType.CRAFTING) return;
            if (event.getInventory().getType() != InventoryType.FURNACE) return;
            if (event.getCurrentItem().equals(LocalCurrencyService.getInstance().isCurrency(itemStack))) {
                Player player = (Player) event.getWhoClicked();
                player.sendMessage(ChatColor.RED + "You can't use currency in smelting recipes.");
                event.setCancelled(true);
                player.closeInventory();

            }
        }
        Logger.getInstance().log("Smelting was not cancelled");
    }
}
