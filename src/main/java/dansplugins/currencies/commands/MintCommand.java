package dansplugins.currencies.commands;

import dansplugins.currencies.CurrencyFactory;
import dansplugins.currencies.MedievalFactionsIntegrator;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.managers.ConfigManager;
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

        boolean powerCostEnabled = ConfigManager.getInstance().getBoolean("powerCostEnabled");
        int powerRequired = -1;
        if (powerCostEnabled) {
            double powerCost = 0.5; // TODO: make power cost a config option
            powerRequired = (int) (amount * powerCost);

            int minimumPowerCost = 1; // TODO: make this a config option
            if (powerRequired < 1) {
                powerRequired = minimumPowerCost;
            }

            int playerPower = MedievalFactionsIntegrator.getInstance().getAPI().getPower(player);

            if (playerPower < powerRequired) {
                player.sendMessage(ChatColor.RED + "You need " + powerRequired + " power to mint that much currency.");
                return false;
            }
            MedievalFactionsIntegrator.getInstance().getAPI().decreasePower(player, powerRequired);
        }

        ItemStack itemStack = CurrencyFactory.getInstance().createCurrencyItem(currency, amount);

        player.getInventory().addItem(itemStack); // TODO: handle full inventory

        if (powerCostEnabled) {
            player.sendMessage(ChatColor.GREEN + "Minted. Power has been decreased by " + powerRequired + ".");
        }
        else {
            player.sendMessage(ChatColor.GREEN + "Minted.");
        }

        return true;
    }

}
