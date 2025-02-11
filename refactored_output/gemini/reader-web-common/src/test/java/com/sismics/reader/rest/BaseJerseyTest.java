```java
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.util.TransactionUtil;
import com.sismics.reader.rest.descriptor.JerseyTestWebAppDescriptorFactory;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.test.framework.JerseyTest;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;

/**
 * Base class of integration tests with Jersey.
 */
public abstract class BaseJerseyTest extends JerseyTest {

    private TestContainer testContainer;

    /**
     * Constructor of BaseJerseyTest.
     */
    public BaseJerseyTest() {
        super(JerseyTestWebAppDescriptorFactory.build());
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        testContainer = TestContainer.Builder.builder(target())
                .build();

        testContainer.startHttpServer();

        TransactionUtil.handle(AppContext.getInstance().getFeedService()::synchronizeAllFeeds);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        testContainer.stopHttpServer();
    }

    /**
     * Simulates a network down situation, e.g. someone having installed the application on his laptop
     * and currently not having connection.
     *
     * @param runnable The code to run while the network is down
     */
    protected void withNetworkDown(Runnable runnable) throws Exception {
        try {
            testContainer.stopHttpServer();

            runnable.run();
        } finally {
            testContainer.startHttpServer();
        }
    }

    /**
     * Creates a user.
     *
     * @param username Username
     */
    public void createUser(String username) {
        // Login admin to create the user
        login("admin", "admin", false);

        // Create the user
        PUT("/user", Map.of(
                "username", username,
                "email", username + "@reader.com",
                "password", "12345678",
                "time_zone", "Asia/Tokyo"
        ));
        assertIsOk();

        // Logout admin
        logout();
    }

    /**
     * Connects a user to the application.
     *
     * @param username Username
     * @param password Password
     * @param remember Remember user
     * @return Authentication token
     */
    public String login(String username, String password, Boolean remember) {
        POST("/user/login", Map.of(
                "username", username,
                "password", password,
                "remember", remember.toString()
        ));
        assertIsOk();
        assertEquals(ClientResponse.Status.OK, ClientResponse.Status.fromStatusCode(statusCode));

        return getAuthenticationCookie();
    }

    public void assertIsOk() {
        assertEquals(ClientResponse.Status.OK, ClientResponse.Status.fromStatusCode(statusCode));
    }

    public void assertIsBadRequest() {
        assertEquals(ClientResponse.Status.BAD_REQUEST, ClientResponse.Status.fromStatusCode(statusCode));
    }

    public void assertIsForbidden() {
        assertEquals(ClientResponse.Status.FORBIDDEN, ClientResponse.Status.fromStatusCode(statusCode));
    }

    /**
     * Connects a user to the application.
     *
     * @param username Username
     * @return Authentication token
     */
    public String login(String username) {
        return login(username, "12345678", false);
    }

    /**
     * Disconnects a user from the application.
     *
     */
    public void logout() {
        POST("/user/logout");
        assertIsOk();
    }

    /**
     * Extracts the authentication token of the response.
     *
     * @return Authentication token
     */
    public String getAuthenticationCookie() {
        String authToken = null;
        for (Cookie cookie : response().getCookies()) {
            if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())) {
                authToken = cookie.getValue();
            }
        }
        return authToken;
    }

    protected ClientResponse GET(String url, Map<String, String> queryParams) {
        return GET(url, new HashMap<String, String>(), queryParams);
    }

    protected ClientResponse GET(String url, Map<String, String> headers, Map<String, String> queryParams) {
        WebResource resource = resource().path(url);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            resource = resource.queryParam(entry.getKey(), entry.getValue());
        }
        statusCode = resource.header(headers).get(ClientResponse.class).getStatus();
        addCookiesFromResponse();
        return response();
    }

    protected ClientResponse GET(String resource) {
        statusCode = GET(resource, new HashMap<String, String>()).getStatus();
        return response();
    }

    protected ClientResponse GET(String resource, Map<String, String> headers) {
        statusCode = GET(resource, headers, new HashMap<String, String>()).getStatus();
        return response();
    }

    protected ClientResponse POST(String url, Map<String, String> putParams) {
        return POST(url, new HashMap<String, String>(), putParams);
    }

    protected ClientResponse POST(String url, Map<String, String> headers, Map<String, String> putParams) {
        statusCode = resource().path(url).header(headers).post(ClientResponse.class, Entity.form(putParams)).getStatus();
        addCookiesFromResponse();
        return response();
    }

    private void addCookiesFromResponse() {
        for (NewCookie cookie : response().getNewCookies()) {
            cookies.put(cookie.getName(), cookie.getValue());
        }
    }

    private static class TestContainer {

        private Wiser wiser;
        private HttpServer httpServer;

        private TestContainer(Wiser wiser, HttpServer httpServer) {
            this.wiser = wiser;
            this.httpServer = httpServer;
        }

        public void startHttpServer() {
            try {
                wiser.start();
                httpServer.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void stopHttpServer() {
            try {
                httpServer.stop();
                wiser.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static TestContainer.Builder builder(String resource) {
            return new TestContainer.Builder(resource);
        }

        static class Builder {
            private Wiser wiser;
            private HttpServer httpServer;
            private String resource = JerseyTestWebAppDescriptorFactory.RESOURCE_PATH;

            public Builder(String resource) {
                this.resource = resource;
            }

            public TestContainer build() {
                if (wiser == null) {
                    wiser = new Wiser();
                    wiser.setPort(2500);
                }
                if (httpServer == null) {
                    httpServer = createHttpServer(wiser, resource);
                }
                return new TestContainer(wiser, httpServer);
            }

            private HttpServer createHttpServer(Wiser wiser, String resource) {
                HttpServer httpServer =