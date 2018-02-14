package model;

/**
 * Created by miha.novak on 06/08/2017.
 */
public class PlaceLimitOrder extends PlaceBaseOrder {

    String price;
    String size;
    String timeInForce;
    String cancelAfter;
    String postOnly;

    public PlaceLimitOrder() {
    }

    public PlaceLimitOrder(String clientOid,
                           String type,
                           String side,
                           String productId,
                           String stp,
                           String price,
                           String size,
                           String timeInForce,
                           String cancelAfter,
                           String postOnly) {
        super(clientOid, type, side, productId, stp);
        this.price = price;
        this.timeInForce = timeInForce;
        this.size = size;
        this.cancelAfter = cancelAfter;
        this.postOnly = postOnly;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }

    public String getCancelAfter() {
        return cancelAfter;
    }

    public void setCancelAfter(String cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public String getPostOnly() {
        return postOnly;
    }

    public void setPostOnly(String postOnly) {
        this.postOnly = postOnly;
    }
}
