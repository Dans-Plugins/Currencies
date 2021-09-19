package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.utils.Logger;
import dansplugins.currencies.managers.ConfigManager;
import dansplugins.currencies.managers.CurrencyManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class CraftingHandler implements Listener {

    @EventHandler()
    public void handle(CraftItemEvent event) {

        if (!ConfigManager.getInstance().getBoolean("disallowCrafting")) {
            Logger.getInstance().log("[DEBUG] Crafting with currencies is allowed.");
            return;
        }

        CraftingInventory inventory = event.getInventory();

        for (ItemStack itemStack : inventory.getMatrix()) {
            if (itemStack == null) {
                continue;
            }
            Logger.getInstance().log("Looking at item: " + itemStack.toString());
            if (CurrencyManager.getInstance().isCurrency(itemStack)) {
                event.getWhoClicked().sendMessage(ChatColor.RED + "You can't use currencies in crafting recipes.");
                event.setCancelled(true);
                return;
            }
        }
        Logger.getInstance().log("Crafting was not cancelled.");
    }

}
