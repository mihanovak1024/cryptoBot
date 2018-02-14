package model;

import java.io.Serializable;

/**
 * Created by miha.novak on 06/08/2017.
 */
public class PlaceBaseOrder implements Serializable {

    public PlaceBaseOrder() {
    }

    public PlaceBaseOrder(String client_oid, String type, String side, String product_id, String stp) {
        this.client_oid = client_oid;
        this.type = type;
        this.side = side;
        this.product_id = product_id;
        this.stp = stp;
    }

    String client_oid;
    String type;
    String side;
    String product_id;
    String stp;

    public String getClient_oid() {
        return client_oid;
    }

    public void setClient_oid(String client_oid) {
        this.client_oid = client_oid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getStp() {
        return stp;
    }

    public void setStp(String stp) {
        this.stp = stp;
    }
}
