package dansplugins.currencies.services;

import dansplugins.currencies.utils.Messenger;
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

public class LocalCurrencyService {
    private static LocalCurrencyService instance;

    // TODO: keep a record of all the currency IDs that have existed so that we don't end up with duplicates when people redo their currencies

    private LocalCurrencyService() {

    }

    public static LocalCurrencyService getInstance() {
        if (instance == null) {
            instance = new LocalCurrencyService();
        }
        return instance;
    }

    public boolean createNewCurrency(String name, MF_Faction faction, Material material) {
        int newCurrencyID = getNewCurrencyID();
        if (newCurrencyID == -1) {
            return false;
        }
        Currency newCurrency = new Currency(name, faction, material, newCurrencyID);
        PersistentData.getInstance().addActiveCurrency(newCurrency);
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
                return factionName; // note: this used to be contained within an if-debug statement
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
                return ID; // note: this used to be contained within an if-debug statement
            }
        }
        return null;
    }

    public void retireCurrency(Currency currency) {
        currency.setRetired(true);
        PersistentData.getInstance().removeActiveCurrency(currency);
        if (currency.getAmount() > 0) {
            PersistentData.getInstance().addRetiredCurrency(currency);
        }
        MF_Faction faction = Currencies.getInstance().getMedievalFactionsAPI().getFaction(currency.getFactionName());
        Messenger.getInstance().sendMessageToOnlinePlayersInFaction(faction, ChatColor.RED + "The currency known as " + currency.getName() + " has been retired.");
    }

    private int getNewCurrencyID() {
        Random random = new Random();
        int numAttempts = 0;
        int maxAttempts = 25;
        int newID = -1;
        do {
            int maxCurrencyIDNumber = LocalConfigService.getInstance().getInt("maxCurrencyIDNumber");
            newID = random.nextInt(maxCurrencyIDNumber);
            numAttempts++;
        } while (isCurrencyIDTaken(newID) && numAttempts <= maxAttempts);
        return newID;
    }

    private boolean isCurrencyIDTaken(int currencyID) {
        return (PersistentData.getInstance().getActiveCurrency(currencyID) != null);
    }
}