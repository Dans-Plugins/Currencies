package dansplugins.currencies.commands;

import dansplugins.currencies.MedievalFactionsIntegrator;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.managers.CurrencyManager;
import dansplugins.currencies.utils.ArgumentParser;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class CreateCommand {
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

        if (PersistentData.getInstance().getCurrency(faction) != null) {
            player.sendMessage(ChatColor.RED + "Your faction already has a currency.");
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /c create (currencyName)");
            return false;
        }

        ArrayList<String> singleQuoteArgs = ArgumentParser.getInstance().getArgumentsInsideSingleQuotes(args);

        if (singleQuoteArgs.size() == 0) {
            player.sendMessage(ChatColor.RED + "Name must be designated between single quotes.");
            return false;
        }

        String name = singleQuoteArgs.get(0);

        ItemStack item = player.getInventory().getItemInMainHand();

        Material material = item.getType();

        if (material == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding an item to use this command.");
            return false;
        }

        boolean success = CurrencyManager.getInstance().createNewCurrency(name, faction, material);
        if (!success) {
            player.sendMessage(ChatColor.RED + "There was a problem creating the currency.");
            return false;
        }

        player.sendMessage(ChatColor.GREEN + "Currency created.");
        return true;
    }
}
