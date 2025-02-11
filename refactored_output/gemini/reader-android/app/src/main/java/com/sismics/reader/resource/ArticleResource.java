```java
import android.content.Context;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Arrays;
import java.util.Set;

/**
 * Interface for interacting with the /article API.
 */
public interface ArticleApi {
    void setReadStatus(String action, Set<String> idList, JsonHttpResponseHandler responseHandler);
}

/**
 * Implementation of the ArticleApi interface using com.loopj.android.httpclient.
 */
class ArticleApiImpl implements ArticleApi {
    private Context context;
    private AsyncHttpClient client = new AsyncHttpClient();

    public ArticleApiImpl(Context context) {
        this.context = context;
    }

    @Override
    public void setReadStatus(String action, Set<String> idList, JsonHttpResponseHandler responseHandler) {
        String apiUrl = getApiUrl(context) + "/article/" + action;
        RequestParams params = new RequestParams();
        params.put("id", idList);

        client.post(apiUrl, params, responseHandler);
    }

    private String getApiUrl(Context context) {
        // TODO: Get API URL from configuration or a dependency injection framework
        return "https://example.com/api";
    }
}

/**
 * Facade for accessing ArticleApi.
 *
 * This class provides a convenient way to access the ArticleApi using a static method.
 * It is responsible for initializing the ArticleApi implementation.
 */
public class ArticleResource {
    private static ArticleApi articleApi;

    public static void init(Context context) {
        articleApi = new ArticleApiImpl(context);
    }

    public static void setReadStatus(String action, Set<String> idList, JsonHttpResponseHandler responseHandler) {
        if (articleApi == null) {
            Log.e("ArticleResource", "ArticleApi is not initialized. Call init() before using this method.");
            return;
        }
        articleApi.setReadStatus(action, idList, responseHandler);
    }
}
```