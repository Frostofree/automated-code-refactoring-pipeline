```java
import com.google.common.io.Closer;
import com.sismics.util.EnvironmentUtil;
import com.sismics.util.cert.CertUtil;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP client.
 *
 * @author jtremeaux
 */
public abstract class ReaderHttpClient<T> {
    /**
     * User-Agent to use.
     * Note: some servers refuse to talk to the default user-agent and issue a 403 Bad Behavior.
     */
    private static final String USER_AGENT = "Mozilla/4.0 (compatible; Like Firefox; SismicsReaderBot/1.0;+http://www.sismics.com/reader/)";

    private static SSLSocketFactory sslSocketFactory;

    static {
        if (EnvironmentUtil.isSslTrustAll()) {
            sslSocketFactory = CertUtil.getTrustAllSocketFactory();
        } else {
            sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    protected int timeout = 20000;

    protected HttpURLConnection createHttpConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
        }
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        return connection;
    }

    public T open(URL url) throws Exception {
        Closer closer = Closer.create();
        try {
            connection = createHttpConnection(url);

            // Handle 3xx redirections
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
                String newUrl = connection.getHeaderField("Location");
                if (newUrl != null) {
                    connection = createHttpConnection(new URL(newUrl));
                }
            }

            return process(closer.register(connection.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                // NOP
            }
        }
    }

    protected abstract T process(InputStream is) throws Exception;
}
```