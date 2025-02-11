```java
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP request utilities.
 * 
 * @author jtremeaux
 */
public class HttpUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * Send an HTTP GET request and return the response body as a string.
     * 
     * @param url The URL to send the request to.
     * @return The response body as a string.
     * @throws IOException If an error occurs while sending the request.
     */
    public static String get(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return readResponse(conn);
    }

    /**
     * Send an HTTP POST request and return the response body as a string.
     * 
     * @param url The URL to send the request to.
     * @param data The data to send in the request body.
     * @return The response body as a string.
     * @throws IOException If an error occurs while sending the request.
     */
    public static String post(URL url, String data) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.getOutputStream().write(data.getBytes());
        return readResponse(conn);
    }
    
    /**
     * Read the response from an HTTP connection and return it as a string.
     * 
     * @param conn The HTTP connection to read the response from.
     * @return The response body as a string.
     * @throws IOException If an error occurs while reading the response.
     */
    private static String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            return sb.toString();
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Error reading HTTP response", e);
            }
            throw e;
        }
    }
}
```