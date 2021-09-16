package dansplugins.currencies.objects;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public interface ICoinpurse {
    UUID getOwnerUUID();
    void setCurrencyAmount(Currency currency, int amount);
    int getCurrencyAmount(Currency currency);
    void sendListOfCurrenciesToPlayer(Player player);
}
