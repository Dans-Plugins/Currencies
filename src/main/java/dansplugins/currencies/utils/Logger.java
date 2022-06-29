package dansplugins.currencies.utils;

import dansplugins.currencies.Currencies;

/**
 * @author Daniel McCoy Stephenson
 */
public class Logger {
    private final Currencies currencies;

    public Logger(Currencies currencies) {
        this.currencies = currencies;
    }

    public void log(String message) {
        if (currencies.isDebugEnabled()) {
            System.out.println("[Currencies] " + message);
        }
    }

}
