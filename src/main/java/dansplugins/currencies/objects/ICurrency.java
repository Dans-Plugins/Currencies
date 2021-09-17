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
    int getAmount();
    void increaseAmount(int amount);
    void decreaseAmount(int amount);
    Map<String, String> save();
    void load(Map<String, String> data);
}
