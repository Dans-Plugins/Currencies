package dansplugins.currencies.objects;

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
    String getDescription();
    void setDescription(String newDescription);
}
