package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.managers.ConfigManager;
import dansplugins.currencies.managers.CurrencyManager;
import dansplugins.currencies.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class FurnaceUsageHandler implements Listener {

    @EventHandler()
    public void handle(CraftItemEvent event) {

        if (!ConfigManager.getInstance().getBoolean("disallowSmelting")) {
            Logger.getInstance().log("[DEBUG] Smelting with currencies is allowed.");
            return;
        }

        CraftingInventory inventory = event.getInventory();

        for (ItemStack itemStack : inventory.getMatrix()) {
            if (itemStack == null) {
                continue;
            }
            Logger.getInstance().log("Looking at item: " + itemStack.toString());
            if (CurrencyManager.getInstance().isCurrency(itemStack)) {
                event.getInventory().getType().equals(InventoryType.FURNACE);
                event.getSlotType().equals(InventoryType.SlotType.CRAFTING);
                event.getWhoClicked().sendMessage(ChatColor.RED + "You can't use currency in smelting recipes.");
                event.setCancelled(true);
                return;
            }
            //We cannot prevent usage of blast furnaces as they were added in v1.14 and Currencies is using 1.13
        }
        Logger.getInstance().log("Smelting was not cancelled");
    }

}
