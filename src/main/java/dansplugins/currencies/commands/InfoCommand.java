package dansplugins.currencies.commands;

import dansplugins.currencies.MedievalFactionsIntegrator;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand {

    public boolean execute(CommandSender sender) {

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

        Currency currency = PersistentData.getInstance().getCurrency(faction);

        if (currency == null) {
            player.sendMessage(ChatColor.RED + "Your faction doesn't have a currency yet.");
            return false;
        }

        player.sendMessage(ChatColor.AQUA + "=== " + currency.getName() + " ===");
        player.sendMessage(ChatColor.AQUA + "Faction: " + currency.getFactionName());
        player.sendMessage(ChatColor.AQUA + "Material: " + currency.getMaterial());
        player.sendMessage(ChatColor.AQUA + "ID: " + currency.getCurrencyID());
        player.sendMessage(ChatColor.AQUA + "Minted: " + currency.getAmount());
        return true;
    }

}
