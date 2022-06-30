package dansplugins.currencies.listeners;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.services.ConfigService;
import dansplugins.currencies.services.CurrencyService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel McCoy Stephenson
 */
public class AnvilUsageListener implements Listener {
    private final ConfigService configService;
    private final CurrencyService currencyService;
    private final Currencies currencies;

    private boolean cooldown = false;

    public AnvilUsageListener(ConfigService configService, CurrencyService currencyService, Currencies currencies) {
        this.configService = configService;
        this.currencyService = currencyService;
        this.currencies = currencies;
    }

    @EventHandler()
    public void handle(PrepareAnvilEvent event) {
        if (!configService.getBoolean("disallowAnvilUsage")) {
            return;
        }

        Player player = (Player) event.getViewers().get(0);

        if (player == null) {
            return;
        }

        Inventory inventory = event.getInventory();
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) {
                continue;
            }
            if (currencyService.isCurrency(itemStack)) {
                if (!cooldown) {
                    player.sendMessage(ChatColor.RED + "You can't use currencies when renaming or repairing.");
                }
                event.setResult(null);
                setCooldown(1);
                return;
            }
        }
    }

    private void setCooldown(int seconds) {
        cooldown = true;
        currencies.getServer().getScheduler().runTaskLater(currencies, new Runnable() {
            @Override
            public void run() {
                cooldown = false;
            }
        }, seconds * 20);
    }

}
