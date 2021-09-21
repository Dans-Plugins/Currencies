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
            sender.sendMessage(ChatColor.RED + "Sub-commands: retire, rename");
            return false;
        }

        String secondaryLabel = args[0];
        String[] arguments = ArgumentParser.getInstance().dropFirstArgument(args);

        if (secondaryLabel.equalsIgnoreCase("retire")) {
            if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.force.retire")) { return false; }
            return forceRetire(sender, arguments);
        }

        if (secondaryLabel.equalsIgnoreCase("rename")) {
            if (!PermissionChecker.getInstance().checkPermission(sender, "currencies.force.rename")) { return false; }
            return forceRename(sender, arguments);
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

    private boolean forceRename(CommandSender sender, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /c force rename 'currencyName' 'newName'");
            return false;
        }

        ArrayList<String> singleQuoteArgs = ArgumentParser.getInstance().getArgumentsInsideSingleQuotes(args);

        if (singleQuoteArgs.size() < 2) {
            sender.sendMessage(ChatColor.RED + "Name and new name must be designated between single quotes.");
            return false;
        }

        String currencyName = singleQuoteArgs.get(0);

        Currency currency = PersistentData.getInstance().getCurrency(currencyName);

        if (currency == null) {
            sender.sendMessage(ChatColor.RED + "There are no currencies named " + currencyName);
            return false;
        }

        String newName = singleQuoteArgs.get(1);

        Currency newNameCurrency = PersistentData.getInstance().getCurrency(newName);

        if (newNameCurrency != null) {
            sender.sendMessage(ChatColor.RED + "That name is taken by an active or retired currency.");
            return false;
        }

        currency.setName(newName);
        sender.sendMessage(ChatColor.GREEN + "Renamed.");
        return true;
    }

}
