package dansplugins.currencies.commands;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.managers.CurrencyManager;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.utils.ArgumentParser;
import dansplugins.currencies.utils.PermissionChecker;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class ForceCommand {

    public boolean execute(CommandSender sender, String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Sub-commands: retire");
            return false;
        }

        String secondaryLabel = args[0];
        String[] arguments = ArgumentParser.getInstance().dropFirstArgument(args);

        if (secondaryLabel.equalsIgnoreCase("retire")) {
            if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.force.retire")) { return false; }
            return forceRetire(sender, arguments);
        }

        sender.sendMessage(ChatColor.RED + "Sub-commands: retire");
        return false;
    }

    private boolean forceRetire(CommandSender sender, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /c force retire 'currencyName'");
            return false;
        }

        ArrayList<String> singleQuoteArgs = ArgumentParser.getInstance().getArgumentsInsideSingleQuotes(args);

        if (singleQuoteArgs.size() == 0) {
            sender.sendMessage(ChatColor.RED + "Name must be designated between single quotes.");
            return false;
        }

        String currencyName = singleQuoteArgs.get(0);

        Currency currency = PersistentData.getInstance().getActiveCurrency(currencyName);

        if (currency == null) {
            sender.sendMessage(ChatColor.RED + "There are no active currencies named " + currencyName);
            return false;
        }

        // TODO: insert an "are you sure?" prompt here

        CurrencyManager.getInstance().retireCurrency(currency);
        sender.sendMessage(ChatColor.GREEN + "Retired.");
        return true;
    }

}