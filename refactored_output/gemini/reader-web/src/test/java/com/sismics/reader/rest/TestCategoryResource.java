```java
package com.sismics.reader.rest;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Exhaustive test of the category resource.
 *
 * @author jtremeaux
 */
public class TestCategoryResource extends BaseJerseyTest {
    private CategoryService categoryServiceMock;

    public TestCategoryResource() {
        categoryServiceMock = mock(CategoryService.class);
    }

    // =========================== Category CRUD Operations =========================== //

    @Test
    public void testCategoryCrud() throws JSONException {
        // Create user 'category1'
        createUser("category1");
        login("category1");

        // Create a category - KO (name required)
        PUT("/category", ImmutableMap.of("name", " "));
        assertIsBadRequest();
        JSONObject json = getJsonResult();
        assertEquals("ValidationError", json.getString("type"));
        assertTrue(json.getString("message"), json.getString("message").contains("more than 1"));

        // Create a category
        PUT("/category", ImmutableMap.of("name", "techno"));
        assertIsOk();
        json = getJsonResult();
        String category1Id = json.optString("id");
        assertNotNull(category1Id);

        // Create a category
        PUT("/category", ImmutableMap.of("name", "comics"));
        assertIsOk();
        json = getJsonResult();
        String category2Id = json.optString("id");
        assertNotNull(category2Id);

        // Update a category
        POST("/category/" + category1Id, ImmutableMap.of(
                "name", "technology",
                "order", "1",
                "folded", Boolean.TRUE.toString()
        ));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));

        // Delete a category
        DELETE("/category/" + category1Id);
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
    }

    // =========================== Subscription Operations =========================== //

    @Test
    public void testSubscriptionOperations() throws JSONException {
        // Create user 'category1'
        createUser("category1");
        login("category1");

        // Subscribe to korben.info
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/korben.xml"));
        assertIsOk();
        JSONObject json = getJsonResult();
        String subscription1Id = json.optString("id");
        assertNotNull(subscription1Id);

        // Subscribe to xkcd.com
        PUT("/subscription", ImmutableMap.of("url", "http://localhost:9997/http/feeds/xkcd.xml"));
        assertIsOk();
        json = getJsonResult();
        String subscription2Id = json.optString("id");
        assertNotNull(subscription2Id);

        // Move the korben.info subscription to "techno"
        POST("/subscription/" + subscription1Id, ImmutableMap.of("category", category1Id));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));

        // Move the xkcd.com subscription to "comics"
        POST("/subscription/" + subscription2Id, ImmutableMap.of("category", category2Id));
        assertIsOk();
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
    }

    // =========================== Unread Article Handling =========================== //

    @Test
    public void testUnreadArticleHandling() throws JSONException {
        // Create user 'category1'
        createUser("category1");
        login("category1");

        // Marks all articles in the technology category as read
        POST("/category/" + category1Id + "/read");
        assertIsOk();
    }

    // =========================== Category Hierarchy Manipulation =========================== //

    @Test
    public void testCategoryHierarchyManipulation() throws JSONException {
        // Create user 'category1'
        createUser("category1");
        login("category1");

        // Get category tree
        GET("/category");
        assertIsOk();
        JSONObject json = getJsonResult();
        JSONArray categories = json.optJSONArray("categories");
        assertNotNull(categories);
        assertEquals(1, categories.length());
        JSONObject rootCategory = categories.optJSONObject(0);
        assertNotNull(rootCategory);

        // Get root category
        GET("/category/" + rootCategory.optString("id"));
        assertIsOk();
        json = getJsonResult();
        JSONArray articles = json.optJSONArray("articles");
        assertNotNull(articles);
        assertEquals(10, articles.length());

        // List all subscriptions
        GET("/subscription");
        assertIsOk();
        json = getJsonResult();
        categories = json.optJSONArray("categories");
        assertNotNull(categories);

        // Get subscriptions for only unread articles
        GET("/subscription", ImmutableMap.of("unread", Boolean.TRUE.toString()));
        assertIsOk();
        json = getJsonResult();
    }
}
```