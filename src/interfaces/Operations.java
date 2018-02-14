package interfaces;

import java.util.Map;

import model.Account;
import utils.AutoDeleteList;

/**
 * Created by miha.novak on 06/08/2017.
 */
public interface Operations {

    boolean buyCrypto(double buyPrice);

    boolean sellCrypto(double sellPrice);

    Map<String, AutoDeleteList<Double>> getCurrentPrices();

    Map<String, Account> getAccountStatus();

    AutoDeleteList<Double> getMyTradesHistory();

    AutoDeleteList<Double> getPricesHistory();
}
