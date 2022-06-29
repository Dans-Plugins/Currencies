package dansplugins.currencies.factories;

import dansplugins.currencies.objects.Currency;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static java.util.Arrays.asList;

/**
 * @author Daniel McCoy Stephenson
 */
public class CurrencyFactory {

    public ItemStack createCurrencyItem(Currency currency, int amount) {
        Material material = Material.getMaterial(currency.getMaterial());
        if (material == null) {
            return null;
        }

        ItemStack newItemStack = new ItemStack(material, amount);
        ItemMeta meta = newItemStack.getItemMeta();
        if (meta == null) {
            return null;
        }

        meta.setDisplayName(currency.getName());
        meta.setLore(asList(
            "",
            ChatColor.WHITE + "Currency of " + currency.getFactionName(),
            ChatColor.WHITE + "currencyID: " + currency.getCurrencyID()
        ));

        newItemStack.setItemMeta(meta);

        return newItemStack;
    }

}