package dansplugins.currencies.commands;

import dansplugins.currencies.factories.CurrencyFactory;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Coinpurse;
import dansplugins.currencies.objects.Currency;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.misc.ArgumentParser;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel McCoy Stephenson
 */
import java.util.ArrayList;
import java.util.Arrays;

public class WithdrawCommand extends AbstractPluginCommand {

    public WithdrawCommand() {
        super(new ArrayList<>(Arrays.asList("withdraw")), new ArrayList<>(Arrays.asList("currencies.withdraw")));
    }

    @Override
    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Usage: /c withdraw 'currency' 'amount'");
        return false;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can't be used in the console.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /c withdraw 'currency' 'amount'");
            return false;
        }

        ArgumentParser argumentParser = new ArgumentParser();
        ArrayList<String> specifiedArguments = argumentParser.getArgumentsInsideDoubleQuotes(args);

        if (specifiedArguments.size() < 2) {
            player.sendMessage(ChatColor.RED + "Arguments must be specified in between quotation marks.");
            return false;
        }

        String currencyName = specifiedArguments.get(0);

        String amountString = specifiedArguments.get(1);
        int amount = Integer.parseInt(amountString); // TODO: handle error here

        if (amount < 0) {
            player.sendMessage(ChatColor.RED + "Amount can't be negative.");
            return false;
        }

        Currency currency = PersistentData.getInstance().getCurrency(currencyName);
        if (currency == null) {
            player.sendMessage(ChatColor.RED + "That currency wasn't found.");
            return false;
        }

        Coinpurse coinpurse = PersistentData.getInstance().getCoinpurse(player.getUniqueId());

        if (coinpurse == null) {
            player.sendMessage(ChatColor.RED + "[Error] Coinpurse not found.");
            return false;
        }

        if (coinpurse.getCurrencyAmount(currency) < amount) {
            player.sendMessage(ChatColor.RED + "Not enough currency.");
            return false;
        }

        // if no free slots then disallow
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Not enough space.");
            return false;
        }

        // withdraw until inventory is full
        int withdrawn = 0;
        for (int i = 0; i < amount; i++) {
            if (!(player.getInventory().firstEmpty() == -1)) {
                coinpurse.subtractCurrencyAmount(currency,1);
                player.getInventory().addItem(CurrencyFactory.getInstance().createCurrencyItem(currency, 1));
                withdrawn++;
            }
            else {
                // player has no free inventory spots
                int remainder = amount - withdrawn;
                if (remainder < 64) {
                    // we can fit this in the last slot
                    coinpurse.subtractCurrencyAmount(currency,remainder);
                    player.getInventory().addItem(CurrencyFactory.getInstance().createCurrencyItem(currency, remainder));
                    withdrawn = withdrawn + remainder;
                    break;
                }
                else {
                    // we can't fit this in the last slot, but we can fit 63
                    coinpurse.subtractCurrencyAmount(currency,63);
                    player.getInventory().addItem(CurrencyFactory.getInstance().createCurrencyItem(currency, 63));
                    withdrawn = withdrawn + 63;
                    break;
                }

            }
        }

        player.sendMessage(ChatColor.GREEN + "Withdrew " + withdrawn + ".");
        return true;
    }
}