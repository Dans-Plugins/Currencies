package dansplugins.currencies.commands;

import dansplugins.currencies.factories.CurrencyFactory;
import dansplugins.currencies.integrators.MedievalFactionsIntegrator;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.services.LocalConfigService;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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

        if (!((boolean)faction.getFlag("officersCanMintCurrency"))) {
            if (!faction.getOwner().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You must be the owner of your faction to use this command.");
                return false;
            }
        }
        else {
            if (!faction.getOwner().equals(player.getUniqueId()) && !faction.isOfficer(player)) {
                player.sendMessage(ChatColor.RED + "You must be an owner or officer to use this command.");
                return false;
            }
        }


        Currency currency = PersistentData.getInstance().getActiveCurrency(faction);

        if (currency == null) {
            player.sendMessage(ChatColor.RED + "Your faction doesn't have a currency yet.");
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /c mint (amount)");
            return false;
        }

        int amount = Integer.parseInt(args[0]); // TODO: handle error here

        if (amount < 0) {
            player.sendMessage(ChatColor.RED + "You can't mint a negative amount of currency.");
            return false;
        }

        boolean powerCostEnabled = LocalConfigService.getInstance().getBoolean("powerCostEnabled");
        int powerRequired = -1;
        if (powerCostEnabled) {
            double powerCost = LocalConfigService.getInstance().getDouble("powerCost");
            powerRequired = (int) (amount * powerCost);

            int minimumPowerCost = 1; LocalConfigService.getInstance().getInt("minimumPowerCost");
            if (powerRequired < 1) {
                powerRequired = minimumPowerCost;
            }

            double playerPower = MedievalFactionsIntegrator.getInstance().getAPI().getPower(player);

            if (playerPower < powerRequired) {
                player.sendMessage(ChatColor.RED + "You need " + powerRequired + " power to mint that much currency.");
                return false;
            }
        }

        if (LocalConfigService.getInstance().getBoolean("itemCost")) {
            // require player to have enough items to mint
            Material material = Material.getMaterial(currency.getMaterial());
            ItemStack itemStack = new ItemStack(material);
            Inventory inventory = player.getInventory();
            if (!inventory.containsAtLeast(itemStack, amount)) {
                player.sendMessage(ChatColor.RED + "You need more " + currency.getMaterial() + ".");
                return false;
            }
            inventory.removeItem(new ItemStack(material, amount));
            MedievalFactionsIntegrator.getInstance().getAPI().decreasePower(player, powerRequired);
        }
        else {
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

        currency.increaseAmount(amount);

        return true;
    }

}
