package dansplugins.currencies.managers;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

public class CurrencyManager {
    private static CurrencyManager instance;

    // TODO: keep a record of all the currency IDs that have existed so that we don't end up with duplicates when people redo their currencies

    private CurrencyManager() {

    }

    public static CurrencyManager getInstance() {
        if (instance == null) {
            instance = new CurrencyManager();
        }
        return instance;
    }

    public boolean createNewCurrency(String name, MF_Faction faction, Material material) {
        int newCurrencyID = getNewCurrencyID();
        if (newCurrencyID == -1) {
            return false;
        }
        Currency newCurrency = new Currency(name, faction, material, newCurrencyID);
        PersistentData.getInstance().addCurrency(newCurrency);
        return true;
    }

    public boolean isCurrency(ItemStack itemStack) {
        if (itemStack.getType() == Material.AIR) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        String currencyIDString = getCurrencyID(itemMeta);
        if (currencyIDString == null) {
            return false;
        }

        int currencyID = Integer.parseInt(currencyIDString); // TODO: handle error here

        return isCurrencyIDTaken(currencyID);
    }

    public String getFactionName(ItemMeta meta) {
        List<String> lore = meta.getLore();
        if (lore == null) {
            return null;
        }
        for (String s : lore) {
            if (s.contains("Currency of")) {
                String factionName = s.substring(14);
                if (Currencies.getInstance().isDebugEnabled()) {
                    return factionName;
                }
            }
        }
        return null;
    }

    public String getCurrencyID(ItemMeta meta) {
        List<String> lore = meta.getLore();
        if (lore == null) {
            return null;
        }
        for (String s : lore) {
            if (s.contains("currencyID")) {
                String ID = s.substring(14);
                if (Currencies.getInstance().isDebugEnabled()) {
                    return ID;
                }
            }
        }
        return null;
    }

    private int getNewCurrencyID() {
        Random random = new Random();
        int numAttempts = 0;
        int maxAttempts = 25;
        int newID = -1;
        do {
            int maxCurrencyIDNumber = ConfigManager.getInstance().getInt("maxCurrencyIDNumber");
            newID = random.nextInt(maxCurrencyIDNumber);
            numAttempts++;
        } while (isCurrencyIDTaken(newID) && numAttempts <= maxAttempts);
        return newID;
    }

    private boolean isCurrencyIDTaken(int currencyID) {
        return (PersistentData.getInstance().getCurrency(currencyID) != null);
    }
}