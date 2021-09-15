package dansplugins.currencies.managers;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.Material;

import java.util.Random;

public class CurrencyManager {
    private static CurrencyManager instance;

    private CurrencyManager() {

    }

    public static CurrencyManager getInstance() {
        if (instance == null) {
            instance = new CurrencyManager();
        }
        return instance;
    }

    public boolean createNewCurrency(String name, MF_Faction faction, Material material) {
        Currency newCurrency = new Currency(name, faction, material, getNewCurrencyID());
        PersistentData.getInstance().addCurrency(newCurrency);
        return true;
    }

    private int getNewCurrencyID() {
        Random random = new Random();

        int newID;
        do {
            newID = random.nextInt(1000000);
        } while (isTaken(newID));
        return newID;
    }

    private boolean isTaken(int newID) {
        return (PersistentData.getInstance().getCurrency(newID) != null);
    }
}