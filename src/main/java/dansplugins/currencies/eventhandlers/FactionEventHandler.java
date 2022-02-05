package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.utils.Logger;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.services.LocalCurrencyService;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.events.FactionDisbandEvent;
import dansplugins.factionsystem.events.FactionRenameEvent;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionEventHandler implements Listener {

    @EventHandler()
    public void handle(FactionRenameEvent event) {

        MF_Faction faction = new MF_Faction(event.getFaction());

        String oldName = event.getCurrentName();
        String newName = event.getProposedName();

        Currency currency = PersistentData.getInstance().getActiveCurrency(faction);

        if (currency == null) {
            return;
        }

        currency.setFactionName(newName);
        Logger.getInstance().log("[DEBUG] Faction Rename Event has been handled.");
    }

    @EventHandler()
    public void disband(FactionDisbandEvent event) {

        MF_Faction faction = new MF_Faction(event.getFaction());
        Currency currency = PersistentData.getInstance().getActiveCurrency(faction);
        LocalCurrencyService.getInstance().retireCurrency(currency);
        Logger.getInstance().log(currency.getName() + " has been retired because " + faction.getName() + " was disbanded.");

    }

}
