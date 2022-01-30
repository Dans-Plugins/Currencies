package dansplugins.currencies.commands;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Coinpurse;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import preponderous.ponder.minecraft.spigot.tools.UUIDChecker;

import java.util.UUID;

public class BalanceCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            // TODO: add message
            return false;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            if (!player.hasPermission("currencies.balance.others")) {
                player.sendMessage(ChatColor.AQUA + "You don't have permission to view the balance information of others.");
                return false;
            }

            String playerName = args[0];
            UUIDChecker uuidChecker = new UUIDChecker();
            UUID playerUUID = uuidChecker.findUUIDBasedOnPlayerName(playerName);
            if (playerUUID == null) {
                player.sendMessage(ChatColor.RED + "That player wasn't found.");
                return false;
            }
            Coinpurse coinpurse = PersistentData.getInstance().getCoinpurse(playerUUID);

            if (coinpurse == null) {
                player.sendMessage(ChatColor.RED + "That player doesn't have a coinpurse yet.");
                return false;
            }

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
