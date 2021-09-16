package dansplugins.currencies.objects;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Coinpurse implements ICoinpurse, Saveable {

    private UUID ownerUUID;
    private HashMap<Currency, Integer> currencyAmounts = new HashMap<>();

    @Override
    public UUID getOwnerUUID() {
        // TODO: implement
        return null;
    }

    @Override
    public void setCurrencyAmount(Currency currency, int amount) {
// TODO: implement
    }

    @Override
    public int getCurrencyAmount(Currency currency) {
        // TODO: implement
        return 0;
    }

    @Override
    public void sendListOfCurrenciesToPlayer(Player player) {
        // TODO: implement
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
