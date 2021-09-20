package dansplugins.currencies.commands;

import dansplugins.currencies.CurrencyFactory;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.managers.ConfigManager;
import dansplugins.currencies.objects.Coinpurse;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.utils.ArgumentParser;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class WithdrawCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            // TODO: add message
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /c withdraw 'currency' 'amount'");
            return false;
        }

        ArrayList<String> singleQuoteArgs = ArgumentParser.getInstance().getArgumentsInsideSingleQuotes(args);

        if (singleQuoteArgs.size() < 2) {
            player.sendMessage(ChatColor.RED + "Arguments must be in between single quotes.");
            return false;
        }

        String currencyName = singleQuoteArgs.get(0);

        String amountString = singleQuoteArgs.get(1);
        int amount = Integer.parseInt(amountString); // TODO: handle error here

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
                int remainder = amount - withdrawn;
                if (remainder < 64) {
                    coinpurse.subtractCurrencyAmount(currency,remainder);
                    player.getInventory().addItem(CurrencyFactory.getInstance().createCurrencyItem(currency, remainder));
                    withdrawn = withdrawn + remainder;
                }
                else {
                    coinpurse.subtractCurrencyAmount(currency,63);
                    player.getInventory().addItem(CurrencyFactory.getInstance().createCurrencyItem(currency, 63));
                    withdrawn = withdrawn + 63;
                    player.sendMessage(ChatColor.RED + "Not enough space.");
                    return false;
                }

            }
        }

        coinpurse.subtractCurrencyAmount(currency, amount);

        player.getInventory().addItem(CurrencyFactory.getInstance().createCurrencyItem(currency, amount));

        player.sendMessage(ChatColor.GREEN + "Withdrew " + withdrawn + ".");
        return true;
    }

}
