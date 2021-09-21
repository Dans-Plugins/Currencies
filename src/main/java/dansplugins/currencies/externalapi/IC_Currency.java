package dansplugins.currencies.externalapi;

public interface IC_Currency {
    String getName();
    String getFactionName();
    String getMaterial();
    int getAmount();
    String getDescription();
    boolean isRetired();
}
