package dansplugins.currencies.commands;

import dansplugins.currencies.MedievalFactionsIntegrator;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Coinpurse;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand {

    public boolean execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            // TODO: add message
            return false;
        }

        Player player = (Player) sender;

        Coinpurse coinpurse = PersistentData.getInstance().getCoinpurse(player.getUniqueId());

        coinpurse.sendCurrencyInformationToPlayer(player);
        return true;
    }

}
