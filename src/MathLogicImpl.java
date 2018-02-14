import java.util.logging.Logger;

import interfaces.MathLogic;
import utils.AutoDeleteList;

/**
 * Created by miha.novak on 04/09/2017.
 */
public class MathLogicImpl implements MathLogic {

    public static final Logger LOGGER = Logger.getLogger(MathLogicImpl.class.getName());

    /**
     * Looking the average price of the past n hours ( 1 < n < 6 )
     * and setting the buy order of averagePrice * 0.995
     * and sell order at averagePrice * 1.005.
     * 1% intake on the buyPrice - sellPrice
     * Aiming towards 1%/
     */

    @Override
    public double getLimitSellPrice(AutoDeleteList<Double> myTradesHistory, AutoDeleteList<Double> pricesHistory) {
        // TODO: 4/09/2017
        return 0;
    }

    @Override
    public double getLimitBuyPrice(AutoDeleteList<Double> myTradesHistory, AutoDeleteList<Double> pricesHistory) {
        // TODO: 4/09/2017
        return 0;
    }
}
