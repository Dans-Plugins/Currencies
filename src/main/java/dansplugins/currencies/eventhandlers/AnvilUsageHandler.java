package dansplugins.currencies.eventhandlers;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.services.LocalConfigService;
import dansplugins.currencies.services.LocalCurrencyService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AnvilUsageHandler implements Listener {

    private boolean cooldown = false;

    @EventHandler()
    public void handle(PrepareAnvilEvent event) {
        if (!LocalConfigService.getInstance().getBoolean("disallowAnvilUsage")) {
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
            if (LocalCurrencyService.getInstance().isCurrency(itemStack)) {
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
        Currencies.getInstance().getServer().getScheduler().runTaskLater(Currencies.getInstance(), new Runnable() {
            @Override
            public void run() {
                cooldown = false;
            }
        }, seconds * 20);
    }

}
