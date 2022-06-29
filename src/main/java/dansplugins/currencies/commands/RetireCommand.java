package dansplugins.currencies.commands;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.services.CurrencyService;
import dansplugins.factionsystem.externalapi.MF_Faction;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel McCoy Stephenson
 */
public class RetireCommand extends AbstractPluginCommand {
    private final Currencies currencies;
    private final PersistentData persistentData;
    private final CurrencyService currencyService;

    public RetireCommand(Currencies currencies, PersistentData persistentData, CurrencyService currencyService) {
        super(new ArrayList<>(Arrays.asList("retire")), new ArrayList<>(Arrays.asList("currencies.retire")));
        this.currencies = currencies;
        this.persistentData = persistentData;
        this.currencyService = currencyService;
    }

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

        if (!faction.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be the owner of your faction to use this command.");
            return false;
        }

        Currency currency = persistentData.getActiveCurrency(faction);

        if (currency == null) {
            player.sendMessage(ChatColor.RED + "Your faction doesn't have a currency yet.");
            return false;
        }

        // TODO: insert an "are you sure?" prompt here

        currencyService.retireCurrency(currency);
        player.sendMessage(ChatColor.GREEN + "Retired.");

        // TODO: inform faction members that the currency has been retired

        return true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        return execute(sender);
    }
}