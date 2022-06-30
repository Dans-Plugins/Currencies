package dansplugins.currencies.listeners;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Coinpurse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Daniel McCoy Stephenson
 */
public class JoinListener implements Listener {
    private final PersistentData persistentData;
    private final Currencies currencies;

    public JoinListener(PersistentData persistentData, Currencies currencies) {
        this.persistentData = persistentData;
        this.currencies = currencies;
    }

    @EventHandler()
    public void handle(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (persistentData.getCoinpurse(player.getUniqueId()) != null) {
            // player already has a coinpurse
            return;
        }

        // player doesn't have a coinpurse
        Coinpurse newCoinpurse = new Coinpurse(persistentData, currencies, player.getUniqueId());
        persistentData.addCoinpurse(newCoinpurse);
        player.sendMessage(ChatColor.AQUA + "You check your side and see that your coinpurse is with you. Type /c help for help."); // TODO: add config option for this message
    }

}
