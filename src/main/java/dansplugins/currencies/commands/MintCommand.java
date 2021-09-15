package dansplugins.currencies.commands;

import dansplugins.currencies.CurrencyFactory;
import dansplugins.currencies.MedievalFactionsIntegrator;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MintCommand {

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
            player.sendMessage(ChatColor.RED + "Your faction doesn't have a currency yet.");
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /c mint (amount)");
            return false;
        }

        int amount = Integer.parseInt(args[0]); // TODO: handle error here

        // TODO: make minting cost power

        ItemStack itemStack = CurrencyFactory.getInstance().createCurrencyItem(currency, amount);

        player.getInventory().addItem(itemStack); // TODO: handle full inventory
        player.sendMessage(ChatColor.GREEN + "Minted.");
        return true;
    }

}
