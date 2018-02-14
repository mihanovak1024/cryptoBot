package model;

/**
 * Created by miha.novak on 05/09/2017.
 */
public class ProductTicker {

    long trade_id;
    String price;
    String size;
    String bide;
    String ask;
    String volume;
    String time;

    public ProductTicker() {
    }

    public long getTrade_id() {
        return trade_id;
    }

    public void setTrade_id(long trade_id) {
        this.trade_id = trade_id;
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

    public String getBide() {
        return bide;
    }

    public void setBide(String bide) {
        this.bide = bide;
    }

    public String getAsk() {
        return ask;
    }

    public void setAsk(String ask) {
        this.ask = ask;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
