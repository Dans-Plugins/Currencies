package dansplugins.currencies.objects;

import java.util.Map;

public interface ICurrency {
    String getName();
    void setName(String name);
    String getFactionName();
    void setFactionName(String factionName);
    String getMaterial();
    void setMaterial(String material);
    int getCurrencyID();
}
