package dansplugins.currencies.externalapi;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.services.LocalCurrencyService;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class CurrenciesAPI implements ICurrenciesAPI {

    @Override
    public C_Currency getCurrency(String currencyName) {
        Currency currency = PersistentData.getInstance().getCurrency(currencyName);
        if (currency != null) {
            return new C_Currency(currency);
        }
        return null;
    }

    @Override
    public C_Currency getCurrency(int currencyID) {
        Currency currency = PersistentData.getInstance().getCurrency(currencyID);
        if (currency != null) {
            return new C_Currency(currency);
        }
        return null;
    }

    @Override
    public C_Currency getCurrency(MF_Faction faction) {
        Currency currency = PersistentData.getInstance().getCurrency(faction);
        if (currency != null) {
            return new C_Currency(currency);
        }
        return null;
    }

    @Override
    public ArrayList<C_Currency> getCurrencies() {
        ArrayList<C_Currency> toReturn = new ArrayList<>();
        for (Currency currency : PersistentData.getInstance().getActiveCurrencies()) {
            toReturn.add(new C_Currency(currency));
        }
        for (Currency currency : PersistentData.getInstance().getActiveCurrencies()) {
            toReturn.add(new C_Currency(currency));
        }
        return toReturn;
    }

    @Override
    public boolean isCurrency(ItemStack itemStack) {
        return LocalCurrencyService.getInstance().isCurrency(itemStack);
    }

    @Override
    public String getCurrencyID(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        return LocalCurrencyService.getInstance().getCurrencyID(meta);
    }
}
