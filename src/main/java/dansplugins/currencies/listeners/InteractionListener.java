package dansplugins.currencies.listeners;

import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.factories.CurrencyFactory;
import dansplugins.currencies.services.CurrencyService;
import dansplugins.currencies.objects.Currency;
import dansplugins.currencies.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Daniel McCoy Stephenson
 */
public class InteractionListener implements Listener {
    private final CurrencyService currencyService;
    private final PersistentData persistentData;
    private final Logger logger;
    private final CurrencyFactory currencyFactory;

    public InteractionListener(CurrencyService currencyService, PersistentData persistentData, Logger logger, CurrencyFactory currencyFactory) {
        this.currencyService = currencyService;
        this.persistentData = persistentData;
        this.logger = logger;
        this.currencyFactory = currencyFactory;
    }

    @EventHandler()
    public void handle(PlayerInteractEvent event) {

        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }

        String currencyIDString = currencyService.getCurrencyID(meta);
        if (currencyIDString == null) {
            return;
        }
        int currencyID = Integer.parseInt(currencyIDString);

        Currency currency = persistentData.getActiveCurrency(currencyID);
        if (currency == null) {
            return;
        }

        String factionName = currencyService.getFactionName(meta);

        if (factionName == null) {
            logger.log("Faction name was null in Interaction Handler.");
            return;
        }

        // fix faction name mismatch if there is one
        logger.log("Comparing '" + currency.getFactionName() + "' to '" + factionName + "'");
        if (!currency.getFactionName().equalsIgnoreCase(factionName)) {
            logger.log("Fixing faction name mismatch with an item stack.");
            event.getPlayer().getInventory().setItemInMainHand(currencyFactory.createCurrencyItem(currency, itemStack.getAmount()));
            event.getPlayer().sendMessage(ChatColor.GREEN + "The currency you're holding had a faction name mismatch. This has been corrected.");
        }

        // fix currency name mismatch if there is one
        if (!currency.getName().equalsIgnoreCase(meta.getDisplayName())) {
            logger.log("Fixing currency name mismatch with an item stack.");
            event.getPlayer().getInventory().setItemInMainHand(currencyFactory.createCurrencyItem(currency, itemStack.getAmount()));
            event.getPlayer().sendMessage(ChatColor.GREEN + "The currency you're holding had a currency name mismatch. This has been corrected.");
        }
    }

}
