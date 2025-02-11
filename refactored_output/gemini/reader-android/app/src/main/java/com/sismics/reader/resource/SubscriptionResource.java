```java
package com.sismics.reader.resource;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sismics.reader.model.Article;
import com.sismics.reader.model.Subscription;
import com.sismics.reader.model.Subscriptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Access to /subscription API.
 *
 * @author bgamard
 */
class SubscriptionResource {

    private static final String TAG = "SubscriptionResource";

    private static final String API_URL = "api/v1/subscription";
    private static RequestQueue requestQueue;

    private SubscriptionResource(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    private static SubscriptionResource instance;

    public static SubscriptionResource getInstance(Context context) {
        if (instance == null) {
            instance = new SubscriptionResource(context);
        }
        return instance;
    }

    /**
     * PUT /subscription.
     *
     * @param subscription Subscription
     * @return Subscription added or null
     */
    @Nullable
    public Subscription add(Subscription subscription) {
        String url = getApiUrl(subscription.getFeedUrl());

        try {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, subscription.toJson(),
                    response -> {
                        if (response.has("id")) {
                            return new Subscription(response.toString());
                        } else {
                            return null;
                        }
                    }, error -> null);

            RequestFuture<Subscription> future = RequestFuture.newFuture();
            requestQueue.add(request);
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Could not add subscription: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * GET /subscription.
     *
     * @param unread True if we want only subscriptions with unread articles
     * @return Subscriptions or null
     */
    @Nullable
    public Subscriptions list(boolean unread) {
        String url = getApiUrl();
        url += "?unread=" + unread;

        try {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        if (response.has("subscriptions")) {
                            JSONArray subscriptions = response.optJSONArray("subscriptions");
                            List<Subscription> list = new ArrayList<>();
                            for (int i = 0; i < subscriptions.length(); i++) {
                                list.add(new Subscription(subscriptions.optJSONObject(i).toString()));
                            }
                            return new Subscriptions(list);
                        } else {
                            return null;
                        }
                    }, error -> null);

            RequestFuture<Subscriptions> future = RequestFuture.newFuture();
            requestQueue.add(request);
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Could not retrieve subscriptions: " + e.getMessage(), e);
            return null;
        }
    }

    private String getApiUrl() {
        return API_URL;
    }

    private String getApiUrl(String feedUrl) {
        try {
            URI uri = new URI(feedUrl);
            String path = uri.getPath();
            return getApiUrl() + path.substring(1);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid feed URL: " + feedUrl, e);
            return getApiUrl();
        }
    }
}

// Moved the articles feed related methods to 'ArticleResource'
```