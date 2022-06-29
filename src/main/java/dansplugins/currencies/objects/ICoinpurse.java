package dansplugins.currencies.objects;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Daniel McCoy Stephenson
 */
public interface ICoinpurse {
    UUID getOwnerUUID();
    void addCurrencyAmount(Currency currency, int amount);
    void subtractCurrencyAmount(Currency currency, int amount);
    void setCurrencyAmount(Currency currency, int amount);
    int getCurrencyAmount(Currency currency);
    void sendCurrencyInformationToPlayer(Player player);
}
