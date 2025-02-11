```java
import com.google.common.collect.ImmutableMap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Unit tests for UserResource.
 */
public class TestUserResource extends BaseJerseyTest {

    /**
     * Test the user resource.
     */
    @Test
    public void testUserResource() throws JSONException {
        JSONObject json;

        createUser("user");
        login("user");

        GET("/user");
        assertIsOk();
        json = getJsonResult();
        String userId = json.optString("id");
        assertNotNull(userId);
        assertEquals("user", json.optString("name"));
        assertEquals("user@example.com", json.optString("email"));

        PUT("/user", new JSONObject().put("name", "updated"));
        assertIsOk();
        json = getJsonResult();
        assertEquals(userId, json.optString("id"));
        assertEquals("updated", json.optString("name"));
        assertEquals("user@example.com", json.optString("email"));

        DELETE("/user");
        assertIsOk();
        json = getJsonResult();
        assertEquals(userId, json.optString("id"));
    }
}

/**
 * Unit tests for SubscriptionResource.
 */
public class TestSubscriptionResource extends BaseJerseyTest {

    /**
     * Test the subscription resource.
     */
    @Test
    public void testSubscriptionResource() throws JSONException {
        JSONObject json;
        JSONArray subscriptions;

        createUser("subscription");
        login("subscription");

        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        subscriptions = json.optJSONArray("subscriptions");
        assertEquals(0, subscriptions.length());

        JSONObject subscription = new JSONObject();
        subscription.put("url", "http://localhost:9997/http/feeds/korben.xml");
        PUT("/subscription", subscription);
        assertIsOk();
        json = getJsonResult();
        String subscriptionId = json.optString("id");
        assertNotNull(subscriptionId);

        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        subscriptions = json.optJSONArray("subscriptions");
        assertEquals(1, subscriptions.length());
        JSONObject subscription0 = subscriptions.optJSONObject(0);
        assertEquals(subscriptionId, subscription0.optString("id"));
        assertEquals("http://localhost:9997/http/feeds/korben.xml", subscription0.optString("url"));

        subscription0.put("url", "http://localhost:9997/http/feeds/korben2.xml");
        PUT("/subscription/" + subscriptionId, subscription0);
        assertIsOk();
        json = getJsonResult();
        assertEquals(subscriptionId, json.optString("id"));
        assertEquals("http://localhost:9997/http/feeds/korben2.xml", json.optString("url"));

        DELETE("/subscription/" + subscriptionId);
        assertIsOk();
        json = getJsonResult();
        assertEquals(subscriptionId, json.optString("id"));
    }
}

/**
 * Unit tests for CategoryResource.
 */
public class TestCategoryResource extends BaseJerseyTest {

    private static final String ROOT_CATEGORY_URL = "/category";

    /**
     * Test the category resource.
     */
    @Test
    public void testCategoryResource() throws JSONException {
        JSONObject json;
        JSONArray categories;

        createUser("category");
        login("category");

        GET(ROOT_CATEGORY_URL);
        assertIsOk();
        json = getJsonResult();
        categories = json.optJSONArray("categories");
        assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        String rootCategoryId = rootCategory.optString("id");
        assertNotNull(rootCategoryId);

        JSONObject category = new JSONObject();
        category.put("name", "new category");
        POST(ROOT_CATEGORY_URL, category);
        assertIsOk();
        json = getJsonResult();
        String categoryId = json.optString("id");
        assertNotNull(categoryId);

        GET(ROOT_CATEGORY_URL);
        assertIsOk();
        json = getJsonResult();
        categories = json.optJSONArray("categories");
        assertEquals(2, categories.length());
        rootCategory = categories.optJSONObject(0);
        assertEquals(rootCategoryId, rootCategory.optString("id"));
        assertEquals("new category", categories.optJSONObject(1).optString("name"));

        category.put("name", "updated category");
        PUT(ROOT_CATEGORY_URL + "/" + categoryId, category);
        assertIsOk();
        json = getJsonResult();
        assertEquals(categoryId, json.optString("id"));
        assertEquals("updated category", json.optString("name"));

        DELETE(ROOT_CATEGORY_URL + "/" + categoryId);
        assertIsOk();
        json = getJsonResult();
        assertEquals(categoryId, json.optString("id"));
    }
}

/**
 * Unit tests for ArticleResource.
 */
public class TestArticleResource extends BaseJerseyTest {

    private static final String ROOT_CATEGORY_URL = "/category";

    /**
     * Test the article resource.
     */
    @Test
    public void testArticleResource() throws JSONException {
        JSONObject json;
        JSONArray articles;
        JSONArray categories;
        JSONObject rootCategory;

        createUser("article");
        login("article");

        JSONObject subscription = new JSONObject();
        subscription.put("url", "http://localhost:9997/http/feeds/korben.xml");
        PUT("/subscription", subscription);
        assertIsOk();
        json = getJsonResult();
        String subscriptionId = json.optString("id");
        assertNotNull(subscriptionId);

        GET(ROOT_CATEGORY_URL);
        assertIsOk();
        json = getJsonResult();
        categories = json.optJSONArray("categories");
        assertEquals(1, categories.length());
        rootCategory = categories.optJSONObject(0);
        String rootCategoryId = rootCategory.optString("id");
        assertNotNull(rootCategoryId);

        GET(ROOT_CATEGORY_URL + "/" + rootCategoryId);
        assertIsOk();
        json = getJsonResult();
        articles = json.optJSONArray("articles");
        assertEquals(10, articles.length());

        GET("/article");
        assertIsOk();
        json = getJsonResult();
        assertEquals(10, json.optInt("unread_count"));

        POST("/article/read");
        assertIsOk();

        GET("/article");
        assertIsOk();
        json = getJsonResult();
        assertEquals(0, json.optInt("unread_count"));
    }
}
```