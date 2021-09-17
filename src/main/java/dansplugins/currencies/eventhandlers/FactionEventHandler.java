package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Currency;
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

        Currency currency = PersistentData.getInstance().getCurrency(faction);

        if (currency == null) {
            return;
        }

        currency.setFactionName(newName);
        if (Currencies.getInstance().isDebugEnabled()) { System.out.println("[DEBUG] Faction Rename Event has been handled."); }
    }

}
