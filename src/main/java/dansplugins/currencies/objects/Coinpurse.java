package dansplugins.currencies.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dansplugins.currencies.Currencies;
import dansplugins.currencies.data.PersistentData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Coinpurse implements ICoinpurse, Savable {

    private UUID ownerUUID;
    private HashMap<Integer, Integer> currencyAmounts = new HashMap<>(); // map of currencyID -> amount

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
    public void subtractCurrencyAmount(Currency currency, int amount) {
        int before = getCurrencyAmount(currency);
        int after = before - amount;
        setCurrencyAmount(currency, after);
    }

    @Override
    public void setCurrencyAmount(Currency currency, int amount) {
        if (!currencyAmounts.containsKey(currency.getCurrencyID())) {
            currencyAmounts.put(currency.getCurrencyID(), amount);
            return;
        }
        currencyAmounts.replace(currency.getCurrencyID(), amount);
    }

    @Override
    public int getCurrencyAmount(Currency currency) {
        if (!currencyAmounts.containsKey(currency.getCurrencyID())) {
            currencyAmounts.put(currency.getCurrencyID(), 0);
            return 0;
        }
        return currencyAmounts.get(currency.getCurrencyID());
    }

    @Override
    public void sendCurrencyInformationToPlayer(Player player) {
        if (currencyAmounts.size() == 0) {
            player.sendMessage(ChatColor.AQUA + "This coinpurse is empty.");
            return;
        }
        player.sendMessage(ChatColor.AQUA + "=== Coinpurse Contents ===");
        for (int currencyID : currencyAmounts.keySet()) {
            Currency currency = PersistentData.getInstance().getCurrency(currencyID);
            if (currency == null) {
                if (Currencies.getInstance().isDebugEnabled()) { System.out.println("[ERROR] Currency was null when attempting to list in the output of the balance command."); }
                continue;
            }
            if (!currency.isRetired()) {
                player.sendMessage(ChatColor.AQUA + currency.getName() + ": " + getCurrencyAmount(currency));
            }
            else {
                player.sendMessage(ChatColor.AQUA + currency.getName() + ": " + getCurrencyAmount(currency) + ChatColor.RED + " [retired]");
            }
        }
    }

    @Override()
    public Map<String, String> save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Map<String, String> saveMap = new HashMap<>();
        saveMap.put("ownerUUID", gson.toJson(ownerUUID));
        saveMap.put("currencyAmounts", gson.toJson(currencyAmounts));

        return saveMap;
    }

    @Override()
    public void load(Map<String, String> data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Type integerToIntegerMapType = new TypeToken<HashMap<Integer, Integer>>(){}.getType();

        ownerUUID = UUID.fromString(gson.fromJson(data.get("ownerUUID"), String.class));
        currencyAmounts = gson.fromJson(data.get("currencyAmounts"), integerToIntegerMapType);
    }
}
