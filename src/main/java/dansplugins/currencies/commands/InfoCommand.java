package dansplugins.currencies.commands;

import dansplugins.currencies.MedievalFactionsIntegrator;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.managers.ConfigManager;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.utils.ArgumentParser;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class InfoCommand {

    public boolean execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            // TODO: add message
            return false;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            if (!player.hasPermission("info.others")) {
                player.sendMessage(ChatColor.AQUA + "You don't have permission to view the currency information of others.");
                return false;
            }

            ArrayList<String> singleQuoteArgs = ArgumentParser.getInstance().getArgumentsInsideSingleQuotes(args);

            if (singleQuoteArgs.size() == 0) {
                player.sendMessage(ChatColor.RED + "Currency name must be designated between single quotes.");
                return false;
            }

            String currencyName = singleQuoteArgs.get(0);
            Currency currency = PersistentData.getInstance().getCurrency(currencyName);

            sendCurrencyInfo(currency, player);
            return true;
        }

        MF_Faction faction = MedievalFactionsIntegrator.getInstance().getAPI().getFaction(player);

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to use this command.");
            return false;
        }

        Currency currency = PersistentData.getInstance().getCurrency(faction);

        if (currency == null) {
            player.sendMessage(ChatColor.RED + "Your faction doesn't have a currency yet.");
            return false;
        }

        sendCurrencyInfo(currency, player);
        return true;
    }

    private void sendCurrencyInfo(Currency currency, Player player) {
        player.sendMessage(ChatColor.AQUA + "=== " + currency.getName() + " ===");
        player.sendMessage(ChatColor.AQUA + "Faction: " + currency.getFactionName());
        player.sendMessage(ChatColor.AQUA + "Description: " + currency.getDescription());
        player.sendMessage(ChatColor.AQUA + "Material: " + currency.getMaterial());
        player.sendMessage(ChatColor.AQUA + "ID: " + currency.getCurrencyID());
        if (ConfigManager.getInstance().getBoolean("showAmountMinted")) {
            player.sendMessage(ChatColor.AQUA + "Minted: " + currency.getAmount());
        }
    }

}
