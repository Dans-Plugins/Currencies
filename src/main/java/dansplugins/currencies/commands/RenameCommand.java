package dansplugins.currencies.commands;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.misc.ArgumentParser;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Daniel McCoy Stephenson
 */
public class RenameCommand extends AbstractPluginCommand {
    private final Currencies currencies;
    private final PersistentData persistentData;

    public RenameCommand(Currencies currencies, PersistentData persistentData) {
        super(new ArrayList<>(Arrays.asList("rename")), new ArrayList<>(Arrays.asList("currencies.rename")));
        this.currencies = currencies;
        this.persistentData = persistentData;
    }

    @Override
    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Usage: /c create (currencyName)");
        return false;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can't be used in the console.");
            return false;
        }

        Player player = (Player) sender;

        MF_Faction faction = currencies.getMedievalFactionsAPI().getFaction(player);

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to use this command.");
            return false;
        }

        if (!faction.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be the owner of your faction to use this command.");
            return false;
        }

        Currency currency = persistentData.getActiveCurrency(faction);
        if (currency == null) {
            player.sendMessage(ChatColor.RED + "Your faction doesn't have a currency.");
            return false;
        }

        ArgumentParser argumentParser = new ArgumentParser();
        List<String> specifiedArguments = argumentParser.getArgumentsInsideDoubleQuotes(args);

        if (specifiedArguments.size() == 0) {
            player.sendMessage(ChatColor.RED + "Name must be specified in between quotation marks.");
            return false;
        }

        String newName = specifiedArguments.get(0);

        if (persistentData.isCurrencyNameTaken(newName)) {
            player.sendMessage(ChatColor.RED + "That name is taken by an active or retired currency.");
            return false;
        }

        currency.setName(newName);
        player.sendMessage(ChatColor.GREEN + "Renamed.");
        return true;
    }
}