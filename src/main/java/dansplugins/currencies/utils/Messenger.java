package dansplugins.currencies.utils;

import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Daniel McCoy Stephenson
 */
public class Messenger {

    public void sendMessageToOnlinePlayersInFaction(MF_Faction faction, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (faction.isMember(player)) {
                player.sendMessage(message);
            }
        }
    }

}
