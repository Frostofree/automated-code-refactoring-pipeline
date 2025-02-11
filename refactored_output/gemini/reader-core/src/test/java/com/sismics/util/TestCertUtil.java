```java
package com.sismics.util;

import com.sismics.util.cert.CertificateValidator;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.fail;

/**
 * Test of the certificate utilities.
 *
 * @author jtremeaux
 */
@Ignore
public class TestCertUtil {

    @Test
    public void sslFailTest() throws Exception {
        try {
            CertificateValidator.testSsl("lescastcodeurs.com", 443);
            fail();
        } catch (Exception e) {
            // NOP
        }
    }

    @Test
    public void sslTrustAllCertificateTest() throws Exception {
        bypassSslValidation();
        CertificateValidator.testSsl("lescastcodeurs.com", 443);
    }

    private static void bypassSslValidation() {
        HttpsURLConnection.setDefaultSSLSocketFactory(
                CertificateValidator.getAllTrustingSslSocketFactory());
    }
}
```