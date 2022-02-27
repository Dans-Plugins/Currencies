package dansplugins.currencies.commands;

import dansplugins.currencies.integrators.MedievalFactionsIntegrator;
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

/**
 * @author Daniel McCoy Stephenson
 */
public class DescCommand extends AbstractPluginCommand {

    public DescCommand() {
        super(new ArrayList<>(Arrays.asList("desc")), new ArrayList<>(Arrays.asList("currencies.desc")));
    }

    @Override
    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Usage: /c desc 'new description'");
        return false;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can't be used in the console.");
            return false;
        }

        Player player = (Player) sender;

        MF_Faction faction = MedievalFactionsIntegrator.getInstance().getAPI().getFaction(player);

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to use this command.");
            return false;
        }

        if (!faction.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be the owner of your faction to use this command.");
            return false;
        }

        Currency currency = PersistentData.getInstance().getActiveCurrency(faction);
        if (currency == null) {
            player.sendMessage(ChatColor.RED + "Your faction doesn't have a currency.");
            return false;
        }

        ArgumentParser argumentParser = new ArgumentParser();
        ArrayList<String> specifiedArguments = argumentParser.getArgumentsInsideDoubleQuotes(args);

        if (specifiedArguments.size() == 0) {
            player.sendMessage(ChatColor.RED + "Description must be designated in between quotation marks.");
            return false;
        }

        String description = specifiedArguments.get(0);

        currency.setDescription(description);
        player.sendMessage(ChatColor.GREEN + "Description set.");
        return true;
    }
}