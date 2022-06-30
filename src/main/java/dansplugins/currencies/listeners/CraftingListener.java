package dansplugins.currencies.listeners;

import dansplugins.currencies.services.ConfigService;
import dansplugins.currencies.services.CurrencyService;
import dansplugins.currencies.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel McCoy Stephenson
 */
public class CraftingListener implements Listener {
    private final ConfigService configService;
    private final Logger logger;
    private final CurrencyService currencyService;

    public CraftingListener(ConfigService configService, Logger logger, CurrencyService currencyService) {
        this.configService = configService;
        this.logger = logger;
        this.currencyService = currencyService;
    }

    @EventHandler()
    public void handle(CraftItemEvent event) {

        if (!configService.getBoolean("disallowCrafting")) {
            logger.log("[DEBUG] Crafting with currencies is allowed.");
            return;
        }

        CraftingInventory inventory = event.getInventory();

        for (ItemStack itemStack : inventory.getMatrix()) {
            if (itemStack == null) {
                continue;
            }
            logger.log("Looking at item: " + itemStack.toString());
            if (currencyService.isCurrency(itemStack)) {
                event.getWhoClicked().sendMessage(ChatColor.RED + "You can't use currencies in crafting recipes.");
                event.setCancelled(true);
                return;
            }
        }
        logger.log("Crafting was not cancelled.");
    }

}
