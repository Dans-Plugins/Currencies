package dansplugins.currencies.listeners;

import dansplugins.currencies.services.ConfigService;
import dansplugins.currencies.services.CurrencyService;
import dansplugins.currencies.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel McCoy Stephenson
 */
public class FurnaceUsageListener implements Listener {
    private final ConfigService configService;
    private final Logger logger;
    private final CurrencyService currencyService;

    public FurnaceUsageListener(ConfigService configService, Logger logger, CurrencyService currencyService) {
        this.configService = configService;
        this.logger = logger;
        this.currencyService = currencyService;
    }

    @EventHandler()
    public void handle(InventoryClickEvent event) {

        if (!configService.getBoolean("disallowSmelting")) {
            logger.log("[DEBUG] Smelting with currencies is allowed.");
            return;
        }

        for (ItemStack itemStack : event.getInventory()) {
            if (itemStack == null) {
                continue;
            }

            if (event.getSlotType() != InventoryType.SlotType.CRAFTING) return;
            if (event.getInventory().getType() != InventoryType.FURNACE) return;
            if (event.getCurrentItem().equals(currencyService.isCurrency(itemStack))) {
                Player player = (Player) event.getWhoClicked();
                player.sendMessage(ChatColor.RED + "You can't use currency in smelting recipes.");
                event.setCancelled(true);
                player.closeInventory();

            }
        }
        logger.log("Smelting was not cancelled");
    }
}
