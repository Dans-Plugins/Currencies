package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.Logger;
import dansplugins.currencies.managers.ConfigManager;
import dansplugins.currencies.managers.CurrencyManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlacementHandler implements Listener {

    @EventHandler()
    public void handle(BlockPlaceEvent event) {
        if (!ConfigManager.getInstance().getBoolean("disallowPlacement")) {
            Logger.getInstance().log("Crafting with currencies is allowed.");
            return;
        }

        if (CurrencyManager.getInstance().isCurrency(event.getItemInHand())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't place currencies.");
            event.setCancelled(true);
            return;
        }
        Logger.getInstance().log("Placement was not cancelled.");
    }

}
