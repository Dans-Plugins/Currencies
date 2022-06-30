package dansplugins.currencies.listeners;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.services.CurrencyService;
import dansplugins.factionsystem.events.FactionDisbandEvent;
import dansplugins.factionsystem.events.FactionRenameEvent;
import dansplugins.factionsystem.externalapi.MF_Faction;
import dansplugins.currencies.utils.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Daniel McCoy Stephenson
 */
public class FactionEventListener implements Listener {
    private final PersistentData persistentData;
    private final Logger logger;
    private final CurrencyService currencyService;

    public FactionEventListener(PersistentData persistentData, Logger logger, CurrencyService currencyService) {
        this.persistentData = persistentData;
        this.logger = logger;
        this.currencyService = currencyService;
    }

    @EventHandler()
    public void handle(FactionRenameEvent event) {

        MF_Faction faction = new MF_Faction(event.getFaction());

        String newName = event.getProposedName();

        Currency currency = persistentData.getActiveCurrency(faction);

        if (currency == null) {
            return;
        }

        currency.setFactionName(newName);
        logger.log("[DEBUG] Faction Rename Event has been handled.");
    }

    @EventHandler()
    public void disband(FactionDisbandEvent event) {

        MF_Faction faction = new MF_Faction(event.getFaction());
        Currency currency = persistentData.getActiveCurrency(faction);
        currencyService.retireCurrency(currency);
        logger.log(currency.getName() + " has been retired because " + faction.getName() + " was disbanded.");

    }

}
