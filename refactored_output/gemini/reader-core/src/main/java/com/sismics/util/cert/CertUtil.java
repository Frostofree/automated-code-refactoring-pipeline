```java
package com.sismics.util.cert;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class CertUtil {

    public static void testSsl(HostConfig hostConfig) {
        InputStream in = null;
        OutputStream out = null;
        SocketFactory socketFactory = hostConfig.getSocketFactory();
        try {
            Socket socket = socketFactory.createSocket(hostConfig.getHostname(), hostConfig.getPort());
            in = socket.getInputStream();
            out = socket.getOutputStream();
            out.write(42);
            while (in.available() > 0) {
                in.read();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // NOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // NOP
                }
            }
        }
    }
}

class TrustAllSSLSocketFactory {
    public static SSLSocketFactory getTrustAllSocketFactory() {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = {new TrustAllManager()};
            sc.init(null, trustManagers, null);
            return sc.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Error creating TrustAllSocketFactory", e);
        }
    }
}
```