package dansplugins.currencies.managers;

import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.Material;

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
        // TODO: implement;
        return true;
    }

    private int getNewCurrencyID() {
        // TODO: implement
        return -1;
    }
}