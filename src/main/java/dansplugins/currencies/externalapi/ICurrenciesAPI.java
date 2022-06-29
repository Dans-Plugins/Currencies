package dansplugins.currencies.externalapi;

import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * @author Daniel McCoy Stephenson
 */
public interface ICurrenciesAPI {
    C_Currency getCurrency(String currencyName);
    C_Currency getCurrency(int currencyID);
    C_Currency getCurrency(MF_Faction faction);
    String getCurrencyID(ItemStack itemStack);
    ArrayList<C_Currency> getCurrencies();
    boolean isCurrency(ItemStack itemStack);
}
