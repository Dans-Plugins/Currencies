package dansplugins.currencies.commands;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Coinpurse;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.minecraft.bukkit.tools.UUIDChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Daniel McCoy Stephenson
 */
public class BalanceCommand extends AbstractPluginCommand {
    private final PersistentData persistentData;

    public BalanceCommand(PersistentData persistentData) {
        super(new ArrayList<>(Arrays.asList("balance")), new ArrayList<>(Arrays.asList("currencies.balance")));
        this.persistentData = persistentData;
    }

    @Override
    public boolean execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can't be used in the console.");
            return false;
        }

        Player player = (Player) sender;

        Coinpurse coinpurse = persistentData.getCoinpurse(player.getUniqueId());

        if (coinpurse == null) {
            player.sendMessage(ChatColor.RED + "[Error] Coinpurse not found.");
            return false;
        }

        coinpurse.sendCurrencyInformationToPlayer(player);
        return true;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can't be used in the console.");
            return false;
        }

        Player player = (Player) sender;

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
        Coinpurse coinpurse = persistentData.getCoinpurse(playerUUID);

        if (coinpurse == null) {
            player.sendMessage(ChatColor.RED + "That player doesn't have a coinpurse yet.");
            return false;
        }

        coinpurse.sendCurrencyInformationToPlayer(player);
        return true;
    }
}