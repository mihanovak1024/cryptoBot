import java.util.Map;
import java.util.logging.Logger;

import interfaces.MathLogic;
import network.BaseUrls;
import utils.AutoDeleteList;

/**
 * Created by miha.novak on 22/06/17.
 */
public class Main {
    public static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private MathLogic mathLogic;
    private BaseUrls baseUrls;

    private int CYCLE_INTERVAL_MINUTES = 5;

    //    Test boolean
    private boolean shouldSell = false;
    private boolean shouldBuy = false;

    public static void main(String[] args) {
        new Main(args);
    }

    /**
     * Cycles for x-times (in future: time interval) and checks the current prices,
     * if the prices are good/bad, it executes a sell/buy order.
     * Checks every y-minutes/seconds for if the circumstances are good for one of the
     * upper mentioned moves.
     *
     * @param args
     */
    public Main(String[] args) {
        mathLogic = new MathLogicImpl();

        baseUrls = new BaseUrls(args.length > 0 && args[0].equals("test"));
        OperationsImpl operations = new OperationsImpl(baseUrls.getRestApiBaseUrl());
        Map<String, AutoDeleteList<Double>> priceList;
        AutoDeleteList<Double> myTradesHistory;
        AutoDeleteList<Double> pricesHistory;
        for (int i = 0; i < 10; i++) {
            LOGGER.info("Cycle since start = " + i + " time since start = " + i * CYCLE_INTERVAL_MINUTES);
            priceList = operations.getCurrentPrices();
            myTradesHistory = operations.getMyTradesHistory();
            pricesHistory = operations.getPricesHistory();
            if (shouldSell) {
                double limitSellPrice = mathLogic.getLimitSellPrice(myTradesHistory, pricesHistory);
                operations.sellCrypto(limitSellPrice);
            } else if (shouldBuy) {
                double limitBuyPrice = mathLogic.getLimitBuyPrice(myTradesHistory, pricesHistory);
                operations.buyCrypto(limitBuyPrice);
            }
            try {
                LOGGER.info(">>>>> PAUSE <<<<<");
                Thread.sleep(CYCLE_INTERVAL_MINUTES * 60 * 1000); // 5min
//                                            Thread.sleep(15 * 1000); // 15sec

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

