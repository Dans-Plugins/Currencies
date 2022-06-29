package dansplugins.currencies.utils;

import dansplugins.currencies.Currencies;
import dansplugins.currencies.services.StorageService;
import org.bukkit.Bukkit;

/**
 * @author Daniel McCoy Stephenson
 */
public class Scheduler {
    private final Logger logger;
    private final Currencies currencies;
    private final StorageService storageService;

    public Scheduler(Logger logger, Currencies currencies, StorageService storageService) {
        this.logger = logger;
        this.currencies = currencies;
        this.storageService = storageService;
    }

    public void scheduleAutosave() {
        logger.log("Scheduling hourly autosave.");
        int delay = 60 * 60; // 1 hour
        int secondsUntilRepeat = 60 * 60; // 1 hour
        Bukkit.getScheduler().scheduleSyncRepeatingTask(currencies, new Runnable() {
            @Override
            public void run() {
                logger.log("Saving. This will happen hourly.");
                storageService.save();
            }
        }, delay * 20, secondsUntilRepeat * 20);
    }

}
