package dansplugins.currencies;

import dansplugins.currencies.objects.Currency;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static java.util.Arrays.asList;

public class CurrencyFactory {

    private static CurrencyFactory instance;

    private CurrencyFactory() {

    }

    public static CurrencyFactory getInstance() {
        if (instance == null) {
            instance = new CurrencyFactory();
        }
        return instance;
    }

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