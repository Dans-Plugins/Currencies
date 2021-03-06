package dansplugins.currencies.commands;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.services.CurrencyService;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.minecraft.bukkit.tools.PermissionChecker;
import preponderous.ponder.misc.ArgumentParser;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Daniel McCoy Stephenson
 */
public class ForceCommand extends AbstractPluginCommand {
    private final PersistentData persistentData;
    private final CurrencyService currencyService;

    private final ArgumentParser argumentParser = new ArgumentParser();
    private final PermissionChecker permissionChecker = new PermissionChecker();

    public ForceCommand(PersistentData persistentData, CurrencyService currencyService) {
        super(new ArrayList<>(Arrays.asList("force")), new ArrayList<>(Arrays.asList("currencies.force")));
        this.persistentData = persistentData;
        this.currencyService = currencyService;
    }

    @Override
    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Sub-commands: retire, rename");
        return false;
    }

    public boolean execute(CommandSender sender, String[] args) {
        String secondaryLabel = args[0];
        
        String[] arguments = argumentParser.dropFirstArgument(args);

        
        if (secondaryLabel.equalsIgnoreCase("retire")) {
            if (!permissionChecker.checkPermission(sender, "currencies.force.retire")) { return false; }
            return forceRetire(sender, arguments);
        }

        if (secondaryLabel.equalsIgnoreCase("rename")) {
            if (!permissionChecker.checkPermission(sender, "currencies.force.rename")) { return false; }
            return forceRename(sender, arguments);
        }

        sender.sendMessage(ChatColor.RED + "Sub-commands: retire, rename");
        return false;
    }

    private boolean forceRetire(CommandSender sender, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /c force retire 'currencyName'");
            return false;
        }

        List<String> specifiedArguments = argumentParser.getArgumentsInsideDoubleQuotes(args);

        if (specifiedArguments.size() == 0) {
            sender.sendMessage(ChatColor.RED + "Name must be specified in between quotation marks.");
            return false;
        }

        String currencyName = specifiedArguments.get(0);

        Currency currency = persistentData.getActiveCurrency(currencyName);

        if (currency == null) {
            sender.sendMessage(ChatColor.RED + "There are no active currencies named " + currencyName);
            return false;
        }

        // TODO: insert an "are you sure?" prompt here

        currencyService.retireCurrency(currency);
        sender.sendMessage(ChatColor.GREEN + "Retired.");
        return true;
    }

    private boolean forceRename(CommandSender sender, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /c force rename 'currencyName' 'newName'");
            return false;
        }

        List<String> specifiedArguments = argumentParser.getArgumentsInsideDoubleQuotes(args);

        if (specifiedArguments.size() < 2) {
            sender.sendMessage(ChatColor.RED + "Name and new name must be designated in between quotation marks.");
            return false;
        }

        String currencyName = specifiedArguments.get(0);

        Currency currency = persistentData.getCurrency(currencyName);

        if (currency == null) {
            sender.sendMessage(ChatColor.RED + "There are no currencies named " + currencyName);
            return false;
        }

        String newName = specifiedArguments.get(1);

        Currency newNameCurrency = persistentData.getCurrency(newName);

        if (newNameCurrency != null) {
            sender.sendMessage(ChatColor.RED + "That name is taken by an active or retired currency.");
            return false;
        }

        currency.setName(newName);
        sender.sendMessage(ChatColor.GREEN + "Renamed.");
        return true;
    }

}
