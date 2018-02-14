import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.google.gson.Gson;

import interfaces.Operations;
import model.Account;
import model.Order;
import model.PlaceBaseOrder;
import model.PlaceLimitOrder;
import model.PlaceMarketOrder;
import model.Product;
import model.ProductTicker;
import network.BaseUrls;
import network.HttpConnection;
import network.HttpConnectionImpl;
import network.NetworkCallback;
import utils.AutoDeleteList;
import utils.WriteToFile;

/**
 * Created by miha.novak on 06/08/2017.
 */
public class OperationsImpl implements Operations, NetworkCallback {
    public static final Logger LOGGER = Logger.getLogger(OperationsImpl.class.getName());

    private final int WAIT_NETWORK_SECONDS = 5;
    private Lock nLock = new ReentrantLock();
    private Condition nCond = nLock.newCondition();

    private HttpConnection httpConnection;
    private Gson gson = new Gson();
    private String result;

    private String baseUrl;

    private Map<String, Account> currentAccounts;
    private Map<String, AutoDeleteList<Double>> currentPrices;
    // last10
    private AutoDeleteList<Double> myTradesHistory;
    private AutoDeleteList<Double> pricesHistory;

    private double lastPrice;

    public OperationsImpl(String baseUrl) {
        httpConnection = new HttpConnectionImpl();
        this.baseUrl = baseUrl;
        // Update account
        getAccountStatus();
        myTradesHistory = new AutoDeleteList<>();
        myTradesHistory.setMaxNumberOfEntries(10);
        pricesHistory = new AutoDeleteList<>();
        pricesHistory.setMaxNumberOfEntries(10);
    }

    @Override
    public void dataReceived(String object) {
        nLock.lock();
        try {
            this.result = object;
            nCond.signal();
        } catch (Exception e) {
        } finally {
            nLock.unlock();
        }
    }

    @Override
    public boolean buyCrypto(double buyPrice) {
        // TODO: 04/09/2017 include calculated price
        getAccountStatus();
        boolean cryptoBought = buyCryptoLogic();
        if (cryptoBought && lastPrice > 0) {
            myTradesHistory.add(lastPrice);
            dataToFile(true, lastPrice);
        }
        lastPrice = 0;
        return cryptoBought;
    }

    @Override
    public boolean sellCrypto(double sellPrice) {
        // TODO: 04/09/2017 include calculated price
        getAccountStatus();
        boolean cryptoSold = sellCryptoLogic();
        if (cryptoSold && lastPrice > 0) {
            myTradesHistory.add(lastPrice);
            dataToFile(false, lastPrice);
        }
        lastPrice = 0;
        return cryptoSold;
    }

    /**
     * Saves the date of the last operation to file,
     * for informational purposes and statistics.
     *
     * @param buy
     * @param price
     */
    private void dataToFile(boolean buy, Double price) {
        DateFormat df = new SimpleDateFormat("dd:MM:YYYY HH:mm:ss:SSS");
        String availableFunds = currentAccounts.get(buy ? "EUR" : "BTC").getAvailable();
        String size = availableFunds;
        if (buy) {
            size = Double.parseDouble(availableFunds) / price + "";
            size = size.substring(0, availableFunds.indexOf(".") + 7);
        }
        String data = (buy ? "BOUGHT" : "SOLD") + " " + price + " " + size + " " + df.format(new Date()) + "\n";
        new Thread(new Runnable() {
            @Override
            public void run() {
                WriteToFile.writeDataToFile("trades.log", data);
            }
        });
    }

    /**
     * Buys the desired crypto.
     * If the order has been successfully placed
     * but not completed, it checks several time frames
     * if the order was bought successfully.
     * @return
     */
    private boolean buyCryptoLogic() {
        // TODO: 04/09/2017 add support for custom currency
        String url = baseUrl + BaseUrls.ORDERS_URL;
        String json = gson.toJson(createNewLimitOrder(true));
        nLock.lock();
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        httpConnection.getPostRequestResult(url, BaseUrls.ORDERS_URL, json, OperationsImpl.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            if (!nCond.await(WAIT_NETWORK_SECONDS, TimeUnit.SECONDS)) {
                LOGGER.info("buy timeout");
                return false;
            } else {
                // Todo: Rejected buy
                if (result.contains("message")) {
                    LOGGER.info("buy rejected = " + result);
                    return false;
                }
                LOGGER.info("buy OK");
                Order order = gson.fromJson(result.toString(), Order.class);
                LOGGER.info("order status = " + order.getStatus());
                if (!orderStatusDone(order.getId())) {
                    cancelOrder(order.getId());
                    return false;
                }
                myTradesHistory.add(Double.parseDouble(order.getPrice()));
                return true;
            }
        } catch (Exception e) {
            LOGGER.info("buy exception");
            e.printStackTrace();
        } finally {
            nLock.unlock();
        }

        // TODO: write
        return false;
    }

    /**
     * Sells the desired crypto.
     * If the order has been successfully placed
     * but not completed, it checks several time frames
     * if the order was sold successfully.
     * @return
     */
    private boolean sellCryptoLogic() {
        // TODO: 04/09/2017 add support for custom currency
        String url = baseUrl + BaseUrls.ORDERS_URL;
        PlaceBaseOrder order = createNewLimitOrder(false);
        nLock.lock();
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        httpConnection.getPostRequestResult(url, BaseUrls.ORDERS_URL, gson.toJson(order), OperationsImpl.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            if (!nCond.await(WAIT_NETWORK_SECONDS, TimeUnit.SECONDS)) {
                LOGGER.info("sell timeout");
                return false;
            } else {
                LOGGER.info("sell OK");

                //                if (result != null) {
                //                    result = null;
                //                    getAccountStatus();
                //                }
                Order order1 = gson.fromJson(result.toString(), Order.class);
                LOGGER.info("order status = " + order1.getStatus());
                if (!orderStatusDone(order1.getId())) {
                    cancelOrder(order1.getId());
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
        } finally {
            nLock.unlock();
        }

        // TODO: write
        return true;
    }

    /**
     * Cancels order of the order hasn't been
     * completed during a specific given time frame.
     * @param orderId
     * @return
     */
    private boolean cancelOrder(String orderId) {
        LOGGER.info("cancel order = " + orderId);
        String url = baseUrl + BaseUrls.ORDERS_URL + "/" + orderId;
        for (int i = 0, waitSeconds = 3; true && i < 5; i++, waitSeconds++) {
            nLock.lock();
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            httpConnection.getGetRequestResult(url, BaseUrls.ORDERS_URL + "/" + orderId, OperationsImpl.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                if (!nCond.await(WAIT_NETWORK_SECONDS, TimeUnit.SECONDS)) {
                    LOGGER.info("cancelOrderTimeout");
                } else {
                    LOGGER.info("order canceled = " + orderId);
                    return true;
                }
            } catch (Exception e) {
                LOGGER.info("orderStatusCatch");
                e.printStackTrace();
            } finally {
                nLock.unlock();
            }
            LOGGER.info("debug-> threadSleep = " + i);
            try {
                Thread.sleep(waitSeconds * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Checks the status of the order every
     * few seconds for at least 15 seconds (+ timeout delay).
     * @param orderId
     * @return
     */
    private boolean orderStatusDone(String orderId) {
        LOGGER.info("orderStatusDone = " + orderId);
        String url = baseUrl + BaseUrls.ORDERS_URL + "/" + orderId;
        for (int i = 0, waitSeconds = 3; true && i < 5; i++, waitSeconds++) {
            LOGGER.info("orderStatusDone seconds = " + waitSeconds + " i=" + i);
            nLock.lock();
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            httpConnection.getGetRequestResult(url, BaseUrls.ORDERS_URL + "/" + orderId, OperationsImpl.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                if (!nCond.await(WAIT_NETWORK_SECONDS, TimeUnit.SECONDS)) {
                    LOGGER.info("orderStatusTimeout = " + i);
                } else {
                    if (result.contains("message")) {
                        LOGGER.info("orderStatusTimeout order was purged");
                        return true;
                    }
                    LOGGER.info("debug-> orderStatusOK = " + i);
                    Order order = gson.fromJson(result.toString(), Order.class);
                    if (order.getStatus() == "done") {
                        LOGGER.info("orderStatusDone true = " + orderId);
                        return true;
                    } else {
                        LOGGER.info("orderStatusDone false");
                    }
                }
            } catch (Exception e) {
                LOGGER.info("orderStatusCatch = " + i);
                e.printStackTrace();
            } finally {
                nLock.unlock();
            }
            LOGGER.info("debug-> threadSleep = " + i);
            try {
                Thread.sleep(waitSeconds * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Returns the current market prices for desired cryptocurrency.
     * @return
     */
    @Override
    public Map<String, AutoDeleteList<Double>> getCurrentPrices() {
        // TODO: 04/09/2017 add support for custom currency
        String url = baseUrl + BaseUrls.PRODUCTS_URL + "/BTC-EUR/book?level=2";
        nLock.lock();
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        httpConnection.getGetRequestResult(url, BaseUrls.PRODUCTS_URL + "/BTC-EUR/book?level=2", OperationsImpl.this);
                    } catch (Exception e) {
                    }
                }
            }).start();
            if (!nCond.await(WAIT_NETWORK_SECONDS, TimeUnit.SECONDS)) {
                return null;
            } else {
//                LOGGER.info("getCurrentPrices result = " + result);
                Product product = gson.fromJson(result.toString(), Product.class);
                currentPrices = new HashMap<>(2);
                currentPrices.put("ASK", new AutoDeleteList<>());
                currentPrices.put("BID", new AutoDeleteList<>());
                for (int i = 0; i < 3; i++) {
                    currentPrices.get("BID").add(Double.parseDouble(product.getBids().get(i).get(0)));
                    currentPrices.get("ASK").add(Double.parseDouble(product.getAsks().get(i).get(0)));
                }
                LOGGER.info("currentPrices = " + currentPrices.toString());
                return currentPrices;
            }
        } catch (Exception e) {
        } finally {
            nLock.unlock();
        }
        return null;
    }

    /**
     * Get's the lest market trade price.
     * @return
     */
    private Double getLastMarketTrade() {
        // TODO: 04/09/2017 add support for custom currency
        nLock.lock();
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        httpConnection.getGetRequestResult(
                                baseUrl + BaseUrls.PRODUCTS_URL + "/BTC-EUR/ticker",
                                BaseUrls.PRODUCTS_URL,
                                OperationsImpl.this);
                    } catch (Exception e) {
                        LOGGER.info("catch");
                        e.printStackTrace();
                    }
                }
            }).start();
            if (!nCond.await(WAIT_NETWORK_SECONDS, TimeUnit.SECONDS)) {
                LOGGER.info("getLastMarketTrade timeout");
                return 0d;
            } else {
                if (result.contains("message")) {
                    LOGGER.info("getLastMarketTrade message = " + result);
                    return 0d;
                }
                ProductTicker productTicker = gson.fromJson(result.toString(), ProductTicker.class);
                String price = productTicker.getPrice();
                return Double.parseDouble(price);
            }
        } catch (Exception e) {
        } finally {
            nLock.unlock();
        }
        return 0d;
    }

    @Override
    public AutoDeleteList<Double> getPricesHistory() {
        pricesHistory.add(0, getLastMarketTrade());
        return pricesHistory;
    }

    @Override
    public AutoDeleteList<Double> getMyTradesHistory() {
        return myTradesHistory;
    }

    @Override
    public Map<String, Account> getAccountStatus() {
        nLock.lock();
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //                        LOGGER.info("getAcc dataGot = " + datagot);
                        httpConnection.getGetRequestResult(baseUrl + BaseUrls.ACCOUNT_URL, BaseUrls.ACCOUNT_URL, OperationsImpl.this);
                    } catch (Exception e) {
                        LOGGER.info("catch");
                        e.printStackTrace();
                    }
                }
            }).start();
            if (!nCond.await(WAIT_NETWORK_SECONDS, TimeUnit.SECONDS)) {
                // Todo: read from file
                //                LOGGER.info("getAcc timeUp dataGot = " + datagot);
                return null;
            } else {
                //                LOGGER.info("getAcc timeOK dataGot = " + datagot);
                Account[] accounts = gson.fromJson(result, Account[].class);
                Map<String, Account> accountTempMap = new HashMap<>();
                for (Account account : accounts) {
                    accountTempMap.put(account.getCurrency(), account);
                }
                currentAccounts = accountTempMap;
                result = null;
            }
        } catch (Exception e) {
        } finally {
            nLock.unlock();
        }
        return currentAccounts;
    }

    PlaceLimitOrder createNewLimitOrder(boolean shouldBuy) {
        String availableFunds = currentAccounts.get(shouldBuy ? "EUR" : "BTC").getAvailable();
        Double price = calculatePrice(shouldBuy);
        lastPrice = price;
        PlaceLimitOrder order = new PlaceLimitOrder();
        order.setType("limit");
        order.setSide(shouldBuy ? "buy" : "sell");
        order.setProduct_id("BTC-EUR");
        order.setPrice(price + "");
        if (shouldBuy) {
            //  availableFunds = availableFunds.substring(0, availableFunds.indexOf(".") + 3);
            String size = Double.parseDouble(availableFunds) / price + "";
            size = size.substring(0, availableFunds.indexOf(".") + 7);
            order.setSize(size);
        } else {
            availableFunds = availableFunds.substring(0, availableFunds.indexOf(".") + 7);
            order.setSize(availableFunds);
        }
        return order;
    }

    PlaceMarketOrder createNewMarketOrder(boolean shouldBuy) {
        PlaceMarketOrder order = new PlaceMarketOrder();
        order.setSide(shouldBuy ? "buy" : "sell");
        order.setType("market");
        order.setProduct_id("BTC-EUR");
        String availableFunds = currentAccounts.get(shouldBuy ? "EUR" : "BTC").getAvailable();
        if (shouldBuy) {
            availableFunds = availableFunds.substring(0, availableFunds.indexOf(".") + 3);
            order.setFunds(availableFunds);
        } else {
            availableFunds = availableFunds.substring(0, availableFunds.indexOf(".") + 7);
            order.setSize(availableFunds);
        }
        return order;
    }

    private Double calculatePrice(boolean buy) {
        Double spread = currentPrices.get("ASK").getFirst() - currentPrices.get("BID").getFirst();
        if (buy) {
            return currentPrices.get("BID").getFirst() + spread > 0.01 ? 0.01 : 0;
        } else {
            return currentPrices.get("ASK").getFirst() - spread > 0.01 ? 0.01 : 0;
        }
    }
}
