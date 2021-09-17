package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.managers.ConfigManager;
import dansplugins.currencies.managers.CurrencyManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlacementHandler implements Listener {

    @EventHandler()
    public void handle(BlockPlaceEvent event) {
        if (!ConfigManager.getInstance().getBoolean("disallowPlacement")) {
            if (Currencies.getInstance().isDebugEnabled()) { System.out.println("[DEBUG] Crafting with currencies is allowed."); }
            return;
        }

        if (CurrencyManager.getInstance().isCurrency(event.getItemInHand())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't place currencies.");
            event.setCancelled(true);
            return;
        }
        if (Currencies.getInstance().isDebugEnabled()) { System.out.println("[DEBUG] Placement was not cancelled."); }
    }

}
