package dansplugins.currencies.listeners;

import dansplugins.currencies.services.ConfigService;
import dansplugins.currencies.services.CurrencyService;
import dansplugins.currencies.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * @author Daniel McCoy Stephenson
 */
public class PlacementListener implements Listener {
    private final ConfigService configService;
    private final Logger logger;
    private final CurrencyService currencyService;

    public PlacementListener(ConfigService configService, Logger logger, CurrencyService currencyService) {
        this.configService = configService;
        this.logger = logger;
        this.currencyService = currencyService;
    }

    @EventHandler()
    public void handle(BlockPlaceEvent event) {
        if (!configService.getBoolean("disallowPlacement")) {
            logger.log("Crafting with currencies is allowed.");
            return;
        }

        if (currencyService.isCurrency(event.getItemInHand())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't place currencies.");
            event.setCancelled(true);
            return;
        }
        logger.log("Placement was not cancelled.");
    }

}
