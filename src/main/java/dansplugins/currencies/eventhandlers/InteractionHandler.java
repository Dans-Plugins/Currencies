package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.MedievalFactionsIntegrator;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.managers.CurrencyManager;
import dansplugins.currencies.objects.Currency;
import dansplugins.factionsystem.externalapi.MF_Faction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InteractionHandler implements Listener {

    @EventHandler()
    public void handle(PlayerInteractEvent event) {

        MF_Faction faction = MedievalFactionsIntegrator.getInstance().getAPI().getFaction(event.getPlayer());
        if (faction == null) {
            return;
        }

        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }

        int currencyID = Integer.parseInt(CurrencyManager.getInstance().getCurrencyIDFromLore(meta));

        Currency currency = PersistentData.getInstance().getCurrency(currencyID);

        if (!currency.getFactionName().equalsIgnoreCase(faction.getName())) {
            currency.setFactionName(faction.getName());
            if (Currencies.getInstance().isDebugEnabled()) { System.out.println("[DEBUG] Fixing faction name mismatch with an item stack."); }
        }
    }

}
