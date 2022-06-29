package dansplugins.currencies.data;

import dansplugins.currencies.objects.Coinpurse;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Daniel McCoy Stephenson
 */
public class PersistentData {
    private final ArrayList<Currency> activeCurrencies = new ArrayList<>();
    private final ArrayList<Currency> retiredCurrencies = new ArrayList<>();
    private final ArrayList<Coinpurse> coinpurses = new ArrayList<>();

    public ArrayList<Currency> getActiveCurrencies() {
        return activeCurrencies;
    }

    public ArrayList<Currency> getRetiredCurrencies() {
        return retiredCurrencies;
    }

    public Currency getCurrency(String currencyName) {
        Currency currency = getActiveCurrency(currencyName);
        if (currency != null) {
            return currency;
        }
        return getRetiredCurrency(currencyName);
    }

    public Currency getCurrency(int currencyID) {
        Currency currency = getActiveCurrency(currencyID);
        if (currency != null) {
            return currency;
        }
        return getRetiredCurrency(currencyID);
    }

    public Currency getCurrency(MF_Faction faction) {
        // TODO: implement
        return null;
    }

    public Currency getActiveCurrency(String currencyName) {
        for (Currency c : activeCurrencies) {
            if (c.getName().equalsIgnoreCase(currencyName)) {
                return c;
            }
        }
        return null;
    }

    public Currency getActiveCurrency(int currencyID) {
        for (Currency c : activeCurrencies) {
            if (c.getCurrencyID() == currencyID) {
                return c;
            }
        }
        return null;
    }

    public Currency getActiveCurrency(MF_Faction faction) {
        for (Currency c : activeCurrencies) {
            if (c.getFactionName().equalsIgnoreCase(faction.getName())) {
                return c;
            }
        }
        return null;
    }

    public Currency getRetiredCurrency(int currencyID) {
        for (Currency c : retiredCurrencies) {
            if (c.getCurrencyID() == currencyID) {
                return c;
            }
        }
        return null;
    }

    public Currency getRetiredCurrency(String currencyName) {
        for (Currency c : retiredCurrencies) {
            if (c.getName().equalsIgnoreCase(currencyName)) {
                return c;
            }
        }
        return null;
    }

    public void addActiveCurrency(Currency newCurrency) {
        if (!isCurrencyNameTaken(newCurrency.getName())) {
            activeCurrencies.add(newCurrency);
        }
    }

    public void removeActiveCurrency(Currency currencyToRemove) {
        activeCurrencies.remove(currencyToRemove);
    }

    public void addRetiredCurrency(Currency newCurrency) {
        if (!isCurrencyNameTaken(newCurrency.getName())) {
            retiredCurrencies.add(newCurrency);
        }
    }

    public void removeRetiredCurrency(Currency currencyToRemove) {
        retiredCurrencies.remove(currencyToRemove);
    }

    public void sendListOfActiveCurrenciesToSender(CommandSender sender) {
        if (activeCurrencies.size() == 0) {
            sender.sendMessage(ChatColor.AQUA + "There are no active currencies at this time.");
            return;
        }
        sender.sendMessage(ChatColor.AQUA + "=== Active Currencies ===");
        for (Currency currency : activeCurrencies) {
            sender.sendMessage(ChatColor.AQUA + currency.getName());
        }
    }

    public void sendListOfRetiredCurrenciesToSender(CommandSender sender) {
        if (retiredCurrencies.size() == 0) {
            sender.sendMessage(ChatColor.AQUA + "There are no retired currencies at this time.");
            return;
        }
        sender.sendMessage(ChatColor.AQUA + "=== Retired Currencies ===");
        for (Currency currency : retiredCurrencies) {
            sender.sendMessage(ChatColor.AQUA + currency.getName());
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

    public boolean isCurrencyNameTaken(String currencyName) {
        return (getActiveCurrency(currencyName) != null || getRetiredCurrency(currencyName) != null);
    }
}