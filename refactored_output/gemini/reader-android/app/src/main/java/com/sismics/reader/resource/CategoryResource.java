```java
package com.sismics.reader.resource;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.sismics.reader.util.ApiResource;
import com.sismics.reader.util.RequestUtil;

/**
 * API Resource for interacting with Category API.
 */
public class CategoryResource extends ApiResource {

    /**
     * The API endpoint for categories.
     */
    private static final String API_ENDPOINT = "category";

    /**
     * Adds a new category to the current user.
     *
     * @param name           The name of the category to be added.
     * @param responseHandler The callback handler for the HTTP request.
     */
    public static void add(String name, ResponseHandlerInterface responseHandler) {
        AsyncHttpClient client = RequestUtil.getClient();
        RequestParams params = new RequestParams();
        params.put("name", name);
        client.put(getApiUrl(API_ENDPOINT), params, responseHandler);
    }

    /**
     * Retrieves all categories of the current user.
     *
     * @param responseHandler The callback handler for the HTTP request.
     */
    public static void list(ResponseHandlerInterface responseHandler) {
        AsyncHttpClient client = RequestUtil.getClient();
        RequestParams params = new RequestParams();
        client.get(getApiUrl(API_ENDPOINT), params, responseHandler);
    }

    /**
     * Deletes a category by its ID.
     *
     * @param id             The unique ID of the category to be deleted.
     * @param responseHandler The callback handler for the HTTP request.
     */
    public static void delete(String id, ResponseHandlerInterface responseHandler) {
        AsyncHttpClient client = RequestUtil.getClient();
        client.delete(getApiUrl(API_ENDPOINT + "/" + id), responseHandler);
    }

    /**
     * Updates an existing category by its ID.
     *
     * @param id             The unique ID of the category to be updated.
     * @param name           The updated name of the category. Can be null.
     * @param order          The updated order of the category. Can be null.
     * @param responseHandler The callback handler for the HTTP request.
     */
    public static void update(String id, String name, Integer order, ResponseHandlerInterface responseHandler) {
        AsyncHttpClient client = RequestUtil.getClient();
        RequestParams params = new RequestParams();
        if (name != null) {
            params.put("name", name);
        }
        if (order != null) {
            params.put("order", Integer.toString(order));
        }
        client.post(getApiUrl(API_ENDPOINT + "/" + id), params, responseHandler);
    }

    /**
     * Cancels all pending API requests.
     */
    public static void cancel() {
        RequestUtil.cancelRequests(API_ENDPOINT);
    }
}
```