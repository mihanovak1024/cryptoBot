package interfaces;

import utils.AutoDeleteList;

/**
 * Created by miha.novak on 04/09/2017.
 */
public interface MathLogic {

    double getLimitSellPrice(AutoDeleteList<Double> myTradesHistory, AutoDeleteList<Double> pricesHistory);

    double getLimitBuyPrice(AutoDeleteList<Double> myTradesHistory, AutoDeleteList<Double> pricesHistory);
}
