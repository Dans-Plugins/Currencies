package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Coinpurse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinHandler implements Listener {

    @EventHandler()
    public void handle(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (PersistentData.getInstance().getCoinpurse(player.getUniqueId()) != null) {
            // player already has a coinpurse
            return;
        }

        // player doesn't have a coinpurse
        Coinpurse newCoinpurse = new Coinpurse(player.getUniqueId());
        PersistentData.getInstance().addCoinpurse(newCoinpurse);
        player.sendMessage(ChatColor.AQUA + "You check your side and see that your coinpurse is with you. Type /c help for help."); // TODO: add config option for this message
    }

}
