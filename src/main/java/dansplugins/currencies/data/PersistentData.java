package dansplugins.currencies.data;

import dansplugins.currencies.objects.Coinpurse;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.UUID;

public class PersistentData {
    private static PersistentData instance;

    private ArrayList<Currency> currencies = new ArrayList<>();
    private ArrayList<Coinpurse> coinpurses = new ArrayList<>();

    private PersistentData() {

    }

    public static PersistentData getInstance() {
        if (instance == null) {
            instance = new PersistentData();
        }
        return instance;
    }

    public ArrayList<Currency> getCurrencies() {
        return currencies;
    }

    public Currency getActiveCurrency(String currencyName) {
        for (Currency c : currencies) {
            if (c.getName().equalsIgnoreCase(currencyName)) {
                if (c.isRetired()) {
                    break;
                }
                return c;
            }
        }
        return null;
    }

    public Currency getActiveCurrency(int currencyID) {
        for (Currency c : currencies) {
            if (c.getCurrencyID() == currencyID) {
                if (c.isRetired()) {
                    break;
                }
                return c;
            }
        }
        return null;
    }

    public Currency getActiveCurrency(MF_Faction faction) {
        for (Currency c : currencies) {
            if (c.getFactionName().equalsIgnoreCase(faction.getName())) {
                if (c.isRetired()) {
                    break;
                }
                return c;
            }
        }
        return null;
    }

    public void addCurrency(Currency newCurrency) {
        if (!currencies.contains(newCurrency)) {
            currencies.add(newCurrency);
        }
    }

    public void removeCurrency(Currency currencyToRemove) {
        currencies.remove(currencyToRemove);
    }

    public void sendListOfActiveCurrenciesToSender(CommandSender sender) {
        if (currencies.size() == 0) {
            sender.sendMessage(ChatColor.AQUA + "There are no active currencies at this time.");
            return;
        }
        sender.sendMessage(ChatColor.AQUA + "=== Currencies ===");
        for (Currency currency : currencies) {
            if (!currency.isRetired()) {
                sender.sendMessage(ChatColor.AQUA + currency.getName());
            }
        }
    }

    public ArrayList<Coinpurse> getCoinpurses() {
        return coinpurses;
    }

    public Coinpurse getCoinpurse(UUID playerUUID) {
        for (Coinpurse coinpurse : coinpurses) {
            if (coinpurse.getOwnerUUID().equals(playerUUID)) {
                return coinpurse;
            }
        }
        return null;
    }

    public void addCoinpurse(Coinpurse newCoinpurse) {
        if (!coinpurses.contains(newCoinpurse)) {
            coinpurses.add(newCoinpurse);
        }
    }
}