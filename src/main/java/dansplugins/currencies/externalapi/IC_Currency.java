package dansplugins.currencies.externalapi;

/**
 * @author Daniel McCoy Stephenson
 */
public interface IC_Currency {
    String getName();
    String getFactionName();
    String getMaterial();
    int getAmount();
    String getDescription();
    boolean isRetired();
}
