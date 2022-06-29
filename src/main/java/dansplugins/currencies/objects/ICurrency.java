package dansplugins.currencies.objects;

/**
 * @author Daniel McCoy Stephenson
 */
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
    boolean isRetired();
    void setRetired(Boolean b);
}
