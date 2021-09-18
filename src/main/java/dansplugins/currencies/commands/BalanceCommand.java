package dansplugins.currencies.commands;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Coinpurse;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.utils.ArgumentParser;
import dansplugins.currencies.utils.UUIDChecker;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class BalanceCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            // TODO: add message
            return false;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            if (!player.hasPermission("balance.others")) {
                player.sendMessage(ChatColor.AQUA + "You don't have permission to view the balance information of others.");
                return false;
            }

            String playerName = args[0];
            UUID playerUUID = UUIDChecker.getInstance().findUUIDBasedOnPlayerName(playerName);
            if (playerUUID == null) {
                player.sendMessage(ChatColor.RED + "That player wasn't found.");
                return false;
            }
            Coinpurse coinpurse = PersistentData.getInstance().getCoinpurse(playerUUID);

            coinpurse.sendCurrencyInformationToPlayer(player);
            return true;
        }

        Coinpurse coinpurse = PersistentData.getInstance().getCoinpurse(player.getUniqueId());

        if (coinpurse == null) {
            player.sendMessage(ChatColor.RED + "[Error] Coinpurse not found.");
            return false;
        }

        coinpurse.sendCurrencyInformationToPlayer(player);
        return true;
    }

}
