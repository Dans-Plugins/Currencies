package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.CurrencyFactory;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.managers.CurrencyManager;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InteractionHandler implements Listener {

    @EventHandler()
    public void handle(PlayerInteractEvent event) {

        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }

        String currencyIDString = CurrencyManager.getInstance().getCurrencyID(meta);
        if (currencyIDString == null) {
            return;
        }
        int currencyID = Integer.parseInt(currencyIDString);

        Currency currency = PersistentData.getInstance().getActiveCurrency(currencyID);
        if (currency == null) {
            return;
        }

        String factionName = CurrencyManager.getInstance().getFactionName(meta);

        if (factionName == null) {
            Logger.getInstance().log("Faction name was null in Interaction Handler.");
            return;
        }

        // fix faction name mismatch if there is one
        Logger.getInstance().log("Comparing '" + currency.getFactionName() + "' to '" + factionName + "'");
        if (!currency.getFactionName().equalsIgnoreCase(factionName)) {
            Logger.getInstance().log("Fixing faction name mismatch with an item stack.");
            event.getPlayer().getInventory().setItemInMainHand(CurrencyFactory.getInstance().createCurrencyItem(currency, itemStack.getAmount()));
            event.getPlayer().sendMessage(ChatColor.GREEN + "The currency you're holding had a faction name mismatch. This has been corrected.");
        }

        // fix currency name mismatch if there is one
        if (!currency.getName().equalsIgnoreCase(meta.getDisplayName())) {
            Logger.getInstance().log("Fixing currency name mismatch with an item stack.");
            event.getPlayer().getInventory().setItemInMainHand(CurrencyFactory.getInstance().createCurrencyItem(currency, itemStack.getAmount()));
            event.getPlayer().sendMessage(ChatColor.GREEN + "The currency you're holding had a currency name mismatch. This has been corrected.");
        }
    }

}
