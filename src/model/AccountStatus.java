package model;

/**
 * Created by miha.novak on 06/08/2017.
 */
public class AccountStatus {

    String currency;
    String numberOfCurrency;
    String lastActionTimestamp;
    String lastAction;
    String price;
    Account[] accounts;

    public AccountStatus(String currency,
                         String numberOfCurrency,
                         String lastActionTimestamp,
                         String lastActio,
                         String price,
                         Account[] accounts) {
        this.currency = currency;
        this.numberOfCurrency = numberOfCurrency;
        this.lastActionTimestamp = lastActionTimestamp;
        this.lastAction = lastAction;
        this.price = price;
        this.accounts = accounts;
    }

    public AccountStatus() {
    }

    public String getCurrency() {
        return currency;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNumberOfCurrency() {
        return numberOfCurrency;
    }

    public void setNumberOfCurrency(String numberOfCurrency) {
        this.numberOfCurrency = numberOfCurrency;
    }

    public String getLastActionTimestamp() {
        return lastActionTimestamp;
    }

    public void setLastActionTimestamp(String lastActionTimestamp) {
        this.lastActionTimestamp = lastActionTimestamp;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public Account[] getAccounts() {
        return accounts;
    }

    public void setAccounts(Account[] accounts) {
        this.accounts = accounts;
    }
}
