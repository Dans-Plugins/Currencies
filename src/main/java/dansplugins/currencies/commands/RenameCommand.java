package dansplugins.currencies.commands;

import dansplugins.currencies.MedievalFactionsIntegrator;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.utils.ArgumentParser;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class RenameCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            // TODO: add message
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

        Currency currency = PersistentData.getInstance().getCurrency(faction);
        if (currency == null) {
            player.sendMessage(ChatColor.RED + "Your faction doesn't have a currency.");
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /c create (currencyName)");
            return false;
        }

        ArrayList<String> singleQuoteArgs = ArgumentParser.getInstance().getArgumentsInsideSingleQuotes(args);

        if (singleQuoteArgs.size() == 0) {
            player.sendMessage(ChatColor.RED + "Name must be designated between single quotes.");
            return false;
        }

        String newName = singleQuoteArgs.get(0);

        currency.setName(newName);
        player.sendMessage(ChatColor.GREEN + "Renamed.");
        return true;
    }

}
