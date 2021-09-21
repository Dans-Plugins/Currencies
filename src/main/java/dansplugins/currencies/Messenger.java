package dansplugins.currencies;

import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Messenger {

    private static Messenger instance;

    private Messenger() {

    }

    public static Messenger getInstance() {
        if (instance == null) {
            instance = new Messenger();
        }
        return instance;
    }

    public void sendMessageToOnlinePlayersInFaction(MF_Faction faction, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (faction.isMember(player)) {
                player.sendMessage(message);
            }
        }
    }

}
