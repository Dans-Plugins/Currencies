package dansplugins.currencies;

import dansplugins.factionsystem.externalapi.MedievalFactionsAPI;
import org.bukkit.Bukkit;

public class MedievalFactionsIntegrator {

    private static MedievalFactionsIntegrator instance;

    private MedievalFactionsAPI mf_api = null;

    private MedievalFactionsIntegrator() {
        if (isMedievalFactionsPresent()) {
            if (Currencies.getInstance().isDebugEnabled()) { System.out.println("[DEBUG] Medieval Factions was found successfully!"); }
            try {
                mf_api = new MedievalFactionsAPI();
                System.out.println("[Currencies] Using Medieval Factions External API " + getAPI().getAPIVersion());
                System.out.println("[Currencies] Installed Version of Medieval Factions: " + getAPI().getVersion());
            }
            catch(NoClassDefFoundError e) {
                System.out.println("[Currencies] There was a problem instantiating the Medieval Factions API. Medieval Factions might need to be updated.");
            }
        }
        else {
            System.out.println("[Currencies] Medieval Factions was not found!");
        }
    }

    public static MedievalFactionsIntegrator getInstance() {
        if (instance == null) {
            instance = new MedievalFactionsIntegrator();
        }
        return instance;
    }

    public boolean isMedievalFactionsAPIAvailable() {
        return isMedievalFactionsPresent() && mf_api != null;
    }

    private boolean isMedievalFactionsPresent() {
        return (Bukkit.getServer().getPluginManager().getPlugin("MedievalFactions") != null);
    }

    public MedievalFactionsAPI getAPI() {
        return mf_api;
    }

}