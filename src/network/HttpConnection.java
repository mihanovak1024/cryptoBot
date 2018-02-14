package network;

/**
 * Created by miha.novak on 23/06/17.
 */
public interface HttpConnection {

    void getPostRequestResult(String url, String requestPath, String json, NetworkCallback callback) throws Exception;

    void getGetRequestResult(String url, String requestPath, NetworkCallback callback) throws Exception;
}
