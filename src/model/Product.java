package model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by miha.novak on 15/07/2017.
 */
public class Product implements Serializable {

    String sequence;
    private List<List<String>> bids;
    private List<List<String>> asks;

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public List<List<String>> getBids() {
        return bids;
    }

    public void setBids(List<List<String>> bids) {
        this.bids = bids;
    }

    public List<List<String>> getAsks() {
        return asks;
    }

    public void setAsks(List<List<String>> asks) {
        this.asks = asks;
    }
}
