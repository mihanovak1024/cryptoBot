package network;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.RuntimeErrorException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by miha.novak on 22/06/17.
 */
public class HttpConnectionImpl implements HttpConnection {

    private static final Logger LOGGER = Logger.getLogger(HttpConnectionImpl.class.getName());
    private static final String TAG = HttpConnectionImpl.class.getSimpleName();

    private static final String UTF8 = "UTF-8";
    private static final int BUFFER_SIZE = 4096;

    private final String API_KEY = "yourGdaxApiKey";
    private final String API_PASSPHRASE = "yourGdaxApiPassphrase";
    private final String API_SECRETKEY = "yourGdaxApiSecretKey";

    private static Mac SHARED_MAC;

    static {
        try {
            SHARED_MAC = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException nsaEx) {
            nsaEx.printStackTrace();
        }
    }

    /**
     * Creates a POST request to desired requestPath with
     * desired body and expects the callback, when the data arrives.
     *
     * @param url
     * @param requestPath
     * @param json
     * @param callback
     * @throws Exception
     */
    @Override
    public void getPostRequestResult(String url, String requestPath, String json, NetworkCallback callback) throws Exception {
        callback.dataReceived(sendRequest(url, requestPath, json));
    }

    /**
     * Creates a GET request to desired requestPath with
     * desired body and expects the callback, when the data arrives.
     * @param url
     * @param requestPath
     * @param callback
     * @throws Exception
     */
    @Override
    public void getGetRequestResult(String url, String requestPath, NetworkCallback callback) throws Exception {
        callback.dataReceived(sendRequest(url, requestPath, null));
    }

    /**
     * Generates a signature and a http request.
     * Sets the appropriate headers and makes the request to the
     * specified fullUrl.
     * When we get the response, we check if the statusCode == 200 (OK),
     * transform the response from input stream into a String and return it.
     *
     * @param fullUrl request url
     * @param requestPath used for signature (also present in fullUrl)
     * @param json used for POST requests, empty if it's a GET request
     * @return String if everything is okay, null otherwise
     * @throws Exception
     */
    private String sendRequest(String fullUrl, String requestPath, String json) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpRequestBase request;
        String method;
        if (json == null) {
            request = new HttpGet(fullUrl);
            method = "GET";
        } else {
            request = new HttpPost(fullUrl);
            ((HttpPost) request).setEntity(new ByteArrayEntity(json.getBytes("UTF-8")));
            method = "POST";
        }

        long timestamp = Instant.now().getEpochSecond(); // seconds
        String signature = generateSignature(requestPath + "", method, json != null ? json : "", timestamp + "");
        if (signature.isEmpty()) {
            LOGGER.info(TAG + " signature empty.");
            return null;
        }
        request.addHeader("accept", "application/json");
        request.addHeader("content-type", "application/json");
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.addHeader("CB-ACCESS-KEY", API_KEY);
        request.addHeader("CB-ACCESS-PASSPHRASE", API_PASSPHRASE);
        request.addHeader("CB-ACCESS-TIMESTAMP", timestamp + "");
        request.addHeader(
                "CB-ACCESS-SIGN", signature
        );
        LOGGER.info(TAG + " request = " + requestPath);
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response == null || response.getStatusLine().getStatusCode() != 200) {
            LOGGER.info(TAG + " request failed");
            return null;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(BUFFER_SIZE);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = 0;
            InputStream is = response.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
            try {
                while ((bytesRead = bis.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                bis.close();
            }
        } finally {
            byteArrayOutputStream.close();
        }
        String responseString = byteArrayOutputStream.toString(UTF8);
        LOGGER.info(TAG + " response = " + responseString);
        if (response == null || response.getStatusLine().getStatusCode() != 200) {
            LOGGER.info(TAG + " request failed");
            return null;
        }
        return responseString;
    }

    /**
     * Generates SHA256 encrypted and Base64 encoded signature of
     * requestPath, method, body, timestamp and secret details of your Gdax account.
     *
     * @param requestPath
     * @param method
     * @param body
     * @param timestamp
     * @return
     */
    public String generateSignature(String requestPath, String method, String body, String timestamp) {
        try {
            String prehash = timestamp + method.toUpperCase() + requestPath + body;
            byte[] secretDecoded = Base64.getDecoder().decode(API_SECRETKEY);
            SecretKeySpec keyspec = new SecretKeySpec(secretDecoded, "HmacSHA256");
            Mac sha256 = (Mac) SHARED_MAC.clone();
            sha256.init(keyspec);
            return Base64.getEncoder().encodeToString(sha256.doFinal(prehash.getBytes()));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new RuntimeErrorException(new Error("Cannot set up authentication headers."));
        } catch (CloneNotSupportedException nsaEx) {
            nsaEx.printStackTrace();
        }
        return "";
    }
}
