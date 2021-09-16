package dansplugins.currencies.data;

import dansplugins.currencies.objects.Coinpurse;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    public Currency getCurrency(String currencyName) {
        for (Currency c : currencies) {
            if (c.getName().equalsIgnoreCase(currencyName)) {
                return c;
            }
        }
        return null;
    }

    public Currency getCurrency(int currencyID) {
        for (Currency c : currencies) {
            if (c.getCurrencyID() == currencyID) {
                return c;
            }
        }
        return null;
    }

    public Currency getCurrency(MF_Faction faction) {
        for (Currency c : currencies) {
            if (c.getFactionName().equalsIgnoreCase(faction.getName())) {
                return c;
            }
        }
        return null;
    }

    public void addCurrency(Currency newCurrency) {
        if (getCurrency(newCurrency.getName()) == null) {
            currencies.add(newCurrency);
        }
    }

    public void removeCurrency(Currency currencyToRemove) {
        if (getCurrency(currencyToRemove.getName()) != null) {
            currencies.remove(currencyToRemove);
        }
    }

    public void sendListOfCurrenciesToSender(CommandSender sender) {
        if (currencies.size() == 0) {
            sender.sendMessage(ChatColor.AQUA + "There are no currencies yet.");
            return;
        }
        sender.sendMessage(ChatColor.AQUA + "=== Currencies ===");
        for (Currency currency : currencies) {
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
}