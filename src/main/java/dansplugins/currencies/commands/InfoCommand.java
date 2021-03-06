package dansplugins.currencies.commands;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.services.ConfigService;
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
public class InfoCommand extends AbstractPluginCommand {
    private final Currencies currencies;
    private final PersistentData persistentData;
    private final ConfigService configService;

    public InfoCommand(Currencies currencies, PersistentData persistentData, ConfigService configService) {
        super(new ArrayList<>(Arrays.asList("info")), new ArrayList<>(Arrays.asList("currencies.info")));
        this.currencies = currencies;
        this.persistentData = persistentData;
        this.configService = configService;
    }

    @Override
    public boolean execute(CommandSender sender) {
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

        Currency currency = persistentData.getActiveCurrency(faction);

        if (currency == null) {
            player.sendMessage(ChatColor.RED + "Your faction doesn't have a currency yet.");
            return false;
        }

        sendCurrencyInfo(currency, player);
        return true;
    }
    
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can't be used in the console.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("currencies.info.others")) {
            player.sendMessage(ChatColor.AQUA + "You don't have permission to view the currency information of others.");
            return false;
        }

        ArgumentParser argumentParser = new ArgumentParser();
        List<String> quotationMarks = argumentParser.getArgumentsInsideDoubleQuotes(args);

        if (quotationMarks.size() == 0) {
            player.sendMessage(ChatColor.RED + "Currency name must be designated in between quotation marks.");
            return false;
        }

        String currencyName = quotationMarks.get(0);
        Currency currency = persistentData.getCurrency(currencyName);

        sendCurrencyInfo(currency, player);
        return true;
    }

    private void sendCurrencyInfo(Currency currency, Player player) {
        if (!currency.isRetired()) {
            player.sendMessage(ChatColor.AQUA + "=== " + currency.getName() + " ===");
        }
        else {
            player.sendMessage(ChatColor.AQUA + "=== " + currency.getName() + " === " + ChatColor.RED + "[retired]");
        }

        player.sendMessage(ChatColor.AQUA + "Description: " + currency.getDescription());
        player.sendMessage(ChatColor.AQUA + "Faction: " + currency.getFactionName());
        player.sendMessage(ChatColor.AQUA + "Material: " + currency.getMaterial());
        player.sendMessage(ChatColor.AQUA + "ID: " + currency.getCurrencyID());
        if (configService.getBoolean("showAmountMinted")) {
            player.sendMessage(ChatColor.AQUA + "Minted: " + currency.getAmount());
        }
    }
}