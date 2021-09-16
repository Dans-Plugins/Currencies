package dansplugins.currencies.objects;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Coinpurse implements ICoinpurse, Savable {

    private UUID ownerUUID;
    private HashMap<Currency, Integer> currencyAmounts = new HashMap<>();

    public Coinpurse(UUID playerUUID) {
        this.ownerUUID = playerUUID;
    }

    public Coinpurse(Map<String, String> data) {
        this.load(data);
    }

    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void addCurrencyAmount(Currency currency, int amount) {
        int before = getCurrencyAmount(currency);
        int after = before + amount;
        setCurrencyAmount(currency, after);
    }

    @Override
    public void setCurrencyAmount(Currency currency, int amount) {
        if (!currencyAmounts.containsKey(currency)) {
            currencyAmounts.put(currency, amount);
            return;
        }
        currencyAmounts.replace(currency, amount);
    }

    @Override
    public int getCurrencyAmount(Currency currency) {
        if (!currencyAmounts.containsKey(currency)) {
            currencyAmounts.put(currency, 0);
            return 0;
        }
        return currencyAmounts.get(currency);
    }

    @Override
    public void sendCurrencyInformationToPlayer(Player player) {
        for (Currency currency : currencyAmounts.keySet()) {
            player.sendMessage(ChatColor.AQUA + "=== Coinpurse Contents ===");
            player.sendMessage(ChatColor.AQUA + currency.getName() + ": " + currencyAmounts.get(currency));
        }
    }

    @Override
    public Map<String, String> save() {
        // TODO: implement
        return null;
    }

    @Override
    public void load(Map<String, String> data) {
        // TODO: implement
    }
}
