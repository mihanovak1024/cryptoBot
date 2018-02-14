package network;

/**
 * Created by miha.novak on 25/06/17.
 */
public class BaseUrls {

    private String restApiBaseUrl = "https://api.gdax.com";

    public BaseUrls(boolean isTestMode) {
        if (isTestMode) {
            restApiBaseUrl = "https://api-public.sandbox.gdax.com";
        }
    }

    public String getRestApiBaseUrl() {
        return restApiBaseUrl;
    }

    public static final String ORDERS_URL = "/orders";
    public static final String ACCOUNT_URL = "/accounts";
    public static final String FILLS_URL = "/fills";
    public static final String PRODUCTS_URL = "/products";
}
