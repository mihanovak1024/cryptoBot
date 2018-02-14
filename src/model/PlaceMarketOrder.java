package model;

/**
 * Created by miha.novak on 06/08/2017.
 */
public class PlaceMarketOrder extends PlaceBaseOrder {

    String size;
    String funds;

    public PlaceMarketOrder() {
    }

    public PlaceMarketOrder(String clientOid, String type, String side, String productId, String stp, String size, String funds) {
        super(clientOid, type, side, productId, stp);
        this.size = size;
        this.funds = funds;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFunds() {
        return funds;
    }

    public void setFunds(String funds) {
        this.funds = funds;
    }
}
