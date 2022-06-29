package dansplugins.currencies.externalapi;

import dansplugins.currencies.objects.Currency;

/**
 * @author Daniel McCoy Stephenson
 */
public class C_Currency implements IC_Currency {

    Currency currency;

    public C_Currency(Currency currency) {
        this.currency = currency;
    }

    @Override
    public String getName() {
        return currency.getName();
    }

    @Override
    public String getFactionName() {
        return currency.getFactionName();
    }

    @Override
    public String getMaterial() {
        return currency.getMaterial();
    }

    @Override
    public int getAmount() {
        return currency.getAmount();
    }

    @Override
    public String getDescription() {
        return currency.getDescription();
    }

    @Override
    public boolean isRetired() {
        return currency.isRetired();
    }

}
