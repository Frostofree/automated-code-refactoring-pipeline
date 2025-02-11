import com.google.common.collect.ImmutableMap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import static junit.framework.Assert.*;

/**
 * Exhaustive test of the subscription resource.
 *
 * @author jtremeaux
 */
public class TestSubscriptionResource extends BaseJerseyTest {
    private String subscriptionId;

    public TestSubscriptionResource() {
        super(SubscriptionResource.class);
    }

    @Before
    public void createUser() throws IOException, JSONException {
        // Create user subscription1
        createUser("subscription1");
        login("subscription1");
    }

    /**
     * Test of the subscription add resource.
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testSubscriptionAddResource() throws IOException, JSONException {
        // Create a category
        JSONObject json = PUT("/category", ImmutableMap.of("name", "techno"));
        assertIsOk();
        String category1Id = json.optString("id");
        assertNotNull(category1Id);

        // Subscribe to korben.info
        json = PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben.xml"));
        assertIsOk();
        subscriptionId = json.getString("id");
        assertNotNull(subscriptionId);

        // Move the korben.info subscription to "techno"
        json = POST("/subscription/" + subscriptionId, ImmutableMap.of("category", category1Id));
        assertIsOk();
        assertEquals("ok", json.getString("status"));

        // List all subscriptions
        json = GET("/subscription");
        assertIsOk();
        int unreadCount = json.optInt("unread_count");
        assertTrue(unreadCount > 0);
        JSONArray categories = json.optJSONArray("categories");
        assertNotNull(categories);
        assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        categories = rootCategory.getJSONArray("categories");
        JSONObject technoCategory = categories.optJSONObject(0);
        JSONArray subscriptions = technoCategory.optJSONArray("subscriptions");
        assertEquals(1, subscriptions.length());
        JSONObject subscription = subscriptions.getJSONObject(0);
        assertEquals(10, subscription.getInt("unread_count"));
        assertEquals("http://localhost:9997/http/feeds/korben.xml", subscription.getString("url"));

        // Check the subscription data
        json = GET("/subscription/" + subscriptionId);
        assertIsOk();
        subscription = json.optJSONObject("subscription");
        assertNotNull(subscription);
        assertEquals("Korben", subscription.optString("title"));
        assertEquals("korben.info", subscription.optString("url"));
        assertEquals("Upgrade your mind", subscription.optString("description"));
        assertEquals("http://localhost:9997/http/feeds/korben.xml", subscription.optString("rss_url"));
        assertNotNull(subscription.optLong("create_date"));
        assertNotNull(subscription.optString("category_id"));
        assertEquals("techno", subscription.optString("category_name"));
        JSONArray articles = json.optJSONArray("articles");
        assertEquals(10, articles.length());
        JSONObject article = articles.optJSONObject(0);
        assertNotNull(article);
        String article0Id = article.getString("id");
        assertNotNull(article0Id);
        JSONObject articleSubscription = article.optJSONObject("subscription");
        assertNotNull(articleSubscription.getString("id"));
        assertNotNull(articleSubscription.getString("title"));
        assertNotNull(article.optString("comment_url"));
        article = (JSONObject) articles.get(1);
        String article1Id = article.getString("id");
        article = (JSONObject) articles.get(2);
        String article2Id = article.getString("id");

        // Check pagination
        json = GET("/subscription/" + subscriptionId, ImmutableMap.of("after_article", article1Id));
        assertIsOk();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(8, articles.length());
        assertEquals(article2Id, article.getString("id"));

        // Update the subscription
        json = POST("/subscription/" + subscriptionId, ImmutableMap.of(
                "order", Integer.valueOf(1).toString(),
                "title", "Korben.info"
        ));
        assertIsOk();
        assertEquals("ok", json.getString("status"));

        // Check the updated subscription data
        json = GET("/subscription/" + subscriptionId);
        assertIsOk();
        subscription = json.optJSONObject("subscription");
        assertNotNull(subscription);
        assertEquals("Korben.info", subscription.optString("title"));
        assertEquals("korben.info", subscription.optString("url"));
        assertEquals("Upgrade your mind", subscription.optString("description"));

        // Marks an article as read
        json = POST("/article/" + article0Id + "/read");
        assertIsOk();
        assertEquals("ok", json.getString("status"));

        // Marks an article as read (2nd time)
        json = POST("/article/" + article0Id + "/read");
        assertIsOk();
        assertEquals("ok", json.getString("status"));

        // Check the subscription data
        json = GET("/subscription/" + subscriptionId, ImmutableMap.of("unread", "true"));
        assertIsOk();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(9, articles.length());

        // Check the subscription data
        json = GET("/subscription/" + subscriptionId, ImmutableMap.of("unread", "false"));
        assertIsOk();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());

        // Check all subscriptions for unread articles
        json = GET("/subscription");
        assertIsOk();
        assertEquals(9, json.optInt("unread_count"));

        // Marks an article as unread
        json = POST("/article/" + article0Id + "/unread");
        assertIsOk();
        assertEquals("ok", json.getString("status"));

        // Marks an article as unread (2nd time)
        json = POST("/article/" + article0Id + "/unread");
        assertIsOk();
        assertEquals("ok", json.getString("status"));

        // Check the subscription data
        json = GET("/subscription/" + subscriptionId, ImmutableMap.of("unread", "true"));
        assertIsOk();
        articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());

        // Marks all articles in this subscription as read
        json = POST("/subscription/" + subscriptionId + "/read");
        assertIsOk();

        // Check all subscriptions for unread articles
        json = GET("/subscription");
        assertIsOk();
        assertEquals(0, json.optInt("unread_count"));

        // Delete the subscription
        json = DELETE("/subscription/" + subscriptionId);
        assertIsOk();
        assertEquals("ok", json.getString("status"));
    }

    /**
     * Test of the article dates.
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testArticleDate() throws IOException, JSONException {
        // Subscribe to future date feed: OK
        json = PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/future_date.xml"));
        assertIsOk();
        String subscriptionId = json.getString("id");
        assertNotNull(subscriptionId);

        // Check the subscription data
        json = GET("/subscription/" + subscriptionId);
        assertIsOk();
        JSONObject subscription = json.optJSONObject("subscription");
        assertNotNull(subscription);
        assertEquals("Feed from the future", subscription.optString("title"));
        JSONArray articles = json.optJSONArray("articles");
        assertEquals(1, articles.length());
        JSONObject article = articles.optJSONObject(0);
        assertNotNull(article);
        String article0Id = article.getString("id");
        assertNotNull(article0Id);
        assertEquals("This is an article from the future", article.getString("title"));
        assertTrue(new Date(article.getLong("date")).before(new Date()));
    }

    private void addUserSubscription(String user, String context) throws IOException, JSONException {
        JSONObject json = GET("/user/" + user, ImmutableMap.of("context", context));
        assertIsOk();
        assertTrue(json.has("id"));
        assertTrue(json.has("email"));
        assertTrue(json.has("name"));
        assertTrue(json.has("interests"));
        assertTrue(json.has("avatar"));
        assertTrue(json.has("password"));
    }
}