package dansplugins.currencies.externalapi;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.services.CurrencyService;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * @author Daniel McCoy Stephenson
 */
public class CurrenciesAPI implements ICurrenciesAPI {
    private final PersistentData persistentData;
    private final CurrencyService currencyService;

    public CurrenciesAPI(PersistentData persistentData, CurrencyService currencyService) {
        this.persistentData = persistentData;
        this.currencyService = currencyService;
    }

    @Override
    public C_Currency getCurrency(String currencyName) {
        Currency currency = persistentData.getCurrency(currencyName);
        if (currency != null) {
            return new C_Currency(currency);
        }
        return null;
    }

    @Override
    public C_Currency getCurrency(int currencyID) {
        Currency currency = persistentData.getCurrency(currencyID);
        if (currency != null) {
            return new C_Currency(currency);
        }
        return null;
    }

    @Override
    public C_Currency getCurrency(MF_Faction faction) {
        Currency currency = persistentData.getCurrency(faction);
        if (currency != null) {
            return new C_Currency(currency);
        }
        return null;
    }

    @Override
    public ArrayList<C_Currency> getCurrencies() {
        ArrayList<C_Currency> toReturn = new ArrayList<>();
        for (Currency currency : persistentData.getActiveCurrencies()) {
            toReturn.add(new C_Currency(currency));
        }
        for (Currency currency : persistentData.getActiveCurrencies()) {
            toReturn.add(new C_Currency(currency));
        }
        return toReturn;
    }

    @Override
    public boolean isCurrency(ItemStack itemStack) {
        return currencyService.isCurrency(itemStack);
    }

    @Override
    public String getCurrencyID(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        return currencyService.getCurrencyID(meta);
    }
}
