```java
package com.sismics.util.cert;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * A factory to create a SSL context that trusts all certificates for client connections.
 */
public class TrustAllClientSSLContextFactory extends SSLContextSpi {

    private X509TrustManager trustManager;

    @Override
    protected void engineInit(KeyManagerFactory kmf, TrustManagerFactory tmf, SecureRandom sr) throws GeneralSecurityException {
        if (tmf == null) {
            trustManager = new TrustAllX509TrustManager();
        } else {
            tmf.init(new KeyStoreBuilder().setKey("password".toCharArray(), "JKS".toCharArray()).setTrust("password".toCharArray(), "JKS".toCharArray()).build());
            trustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        }
        super.engineInit(kmf, new TrustManager[]{trustManager}, sr);
    }

    @Override
    protected SSLSocketFactory engineGetSocketFactory() {
        return new SSLSocketFactory() {
            @Override
            public String[] getDefaultCipherSuites() {
                return super.getDefaultCipherSuites();
            }

            @Override
            public String[] getSupportedCipherSuites() {
                return super.getSupportedCipherSuites();
            }

            @Override
            public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
                return super.createSocket(s, host, port, autoClose);
            }

            @Override
            public Socket createSocket(String host, int port) throws IOException {
                return super.createSocket(host, port);
            }

            @Override
            public Socket createSocket(InetAddress host, int port) throws IOException {
                return super.createSocket(host, port);
            }
        };
    }

    @Override
    protected SSLSessionContext engineGetClientSessionContext() {
        return super.engineGetClientSessionContext();
    }

    /**
     * A factory to create a SSL context that trusts all certificates for server connections
     */
    public static class TrustAllServerSSLContextFactory extends TrustAllClientSSLContextFactory {

        @Override
        protected SSLSocketFactory engineGetSocketFactory() {
            return null;
        }

        @Override
        protected SSLSessionContext engineGetClientSessionContext() {
            return null;
        }

        @Override
        protected SSLSessionContext engineGetServerSessionContext() {
            return super.engineGetClientSessionContext();
        }
    }

    private static class TrustAllX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {}

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class KeyStoreBuilder {

        private String password;
        private String type;

        public KeyStoreBuilder setPassword(char[] password) {
            this.password = new String(password);
            return this;
        }

        public KeyStoreBuilder setType(char[] type) {
            this.type = new String(type);
            return this;
        }

        public KeyStore build() throws GeneralSecurityException {
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(null, password.toCharArray());
            return keyStore;
        }
    }
}
```