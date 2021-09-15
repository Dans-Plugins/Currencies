package dansplugins.currencies.objects;

import org.bukkit.Material;

public class Currency {
    private String name;
    private String factionName;
    private String material;
    private int currencyID;

    public Currency(String name, String factionName, Material material, int currencyID) {
        this.name = name;
        this.factionName = factionName;
        this.material = material.name();
        this.currencyID = currencyID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFactionName() {
        return factionName;
    }

    public void setFactionName(String factionName) {
        this.factionName = factionName;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getCurrencyID() {
        return currencyID;
    }
}