package dansplugins.currencies.utils;

import dansplugins.currencies.Currencies;

public class Logger {

    private static Logger instance;

    private Logger() {

    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void log(String message) {
        if (Currencies.getInstance().isDebugEnabled()) {
            System.out.println("[Currencies] " + message);
        }
    }

}
