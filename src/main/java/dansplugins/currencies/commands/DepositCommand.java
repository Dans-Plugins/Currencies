package dansplugins.currencies.commands;

import dansplugins.currencies.CurrencyFactory;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.managers.CurrencyManager;
import dansplugins.currencies.objects.Coinpurse;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.utils.ArgumentParser;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class DepositCommand {

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            // TODO: add message
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /c deposit 'currency' 'amount'");
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

        if (!player.getInventory().containsAtLeast(CurrencyFactory.getInstance().createCurrencyItem(currency, 1), amount)) {
            player.sendMessage(ChatColor.RED + "Not enough currency.");
            return false;
        }

        coinpurse.addCurrencyAmount(currency, amount);

        player.getInventory().removeItem(CurrencyFactory.getInstance().createCurrencyItem(currency, amount));

        player.sendMessage(ChatColor.GREEN + "Deposited.");
        return true;
    }

}
