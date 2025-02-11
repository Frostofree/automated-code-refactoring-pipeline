```java
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.sismics.reader.listener.ArticlesHelperListener;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SharedArticlesHelper {
    private SharedArticlesState state;
    private List<ArticlesHelperListener> listeners;
    private ArticlesAdapterHandler adapterHandler;

    public SharedArticlesHelper(String url, boolean unread) {
        this.state = new SharedArticlesState(url, unread);
    }

    public void addListener(ArticlesHelperListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void removeListener(ArticlesHelperListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public boolean isFullyLoaded() {
        return state.isFullyLoaded();
    }

    public void load(Context context) {
        state.load(context);
    }

    public ArticlesAdapterHandler createAdapterHandler(ArrayAdapter<JSONObject> adapter, Context context) {
        if (adapterHandler == null) {
            adapterHandler = new ArticlesAdapterHandler(adapter, context, state);
        }
        return adapterHandler;
    }

    private void onDataChanged() {
        if (listeners != null) {
            for (ArticlesHelperListener listener : listeners) {
                listener.onDataChanged();
            }
        }
    }

    public interface ArticlesAdapterHandler {
        View getView(int position, View convertView, ViewGroup parent);

        void updateState(SharedArticlesState state);
    }

    private static class ArticlesAdapterHandlerImpl implements ArticlesAdapterHandler {
        private final ArrayAdapter<JSONObject> adapter;
        private final Context context;
        private final SharedArticlesState state;

        public ArticlesAdapterHandlerImpl(ArrayAdapter<JSONObject> adapter, Context context, SharedArticlesState state) {
            this.adapter = adapter;
            this.context = context;
            this.state = state;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Your getView implementation goes here
            return null;
        }

        @Override
        public void updateState(SharedArticlesState state) {
            this.state.setArticleItems(state.getArticleItems());
            this.state.setFullyLoaded(state.isFullyLoaded());
            this.adapter.notifyDataSetChanged();
        }
    }

    private static class SharedArticlesState {
        private final String url;
        private final boolean unread;
        private boolean loading;
        private boolean fullyLoaded;
        private List<JSONObject> articleItems;

        public SharedArticlesState(String url, boolean unread) {
            this.url = url;
            this.unread = unread;
        }

        public List<JSONObject> getArticleItems() {
            return articleItems != null ? articleItems : Collections.emptyList();
        }

        public void setArticleItems(List<JSONObject> articleItems) {
            this.articleItems = articleItems;
        }

        public boolean isFullyLoaded() {
            return fullyLoaded;
        }

        public void setFullyLoaded(boolean fullyLoaded) {
            this.fullyLoaded = fullyLoaded;
        }

        public void load(Context context) {
            if (loading) {
                return;
            }

            loading = true;

            // Number of articles to fetch
            int articlesFetchedPref = PreferenceUtil.getIntegerPreference(context, PreferenceUtil.PREF_ARTICLES_FETCHED, 10);

            String afterArticleId = null;
            if (articleItems != null && articleItems.size() > 0) {
                afterArticleId = articleItems.get(articleItems.size() - 1).optString("id");
            }

            SubscriptionResource.feed(context, url, unread, url.startsWith("/search/") ? articleItems.size() : -1,
                    articlesFetchedPref, afterArticleId, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject json) {
                            loading = false;

                            JSONArray articles = json.optJSONArray("articles");
                            if (articles.length() == 0) {
                                fullyLoaded = true;
                            }

                            addArticles(articles);
                        }

                        @Override
                        public void onFailure(final int statusCode, final Header[] headers, final byte[] responseBytes, final Throwable throwable) {
                            loading = false;

                            // Tell the listeners something bad happened
                        }

                        private void addArticles(JSONArray articles) {
                            if (articleItems == null) {
                                articleItems = new ArrayList<>();
                            }

                            for (int i = 0; i < articles.length(); i++) {
                                JSONObject article = articles.optJSONObject(i);

                                // Precompute some data
                                try {
                                    String description = article.optString("description");
                                    String cleanedDescription = description.replaceAll("<.*?>", "").trim();
                                    int length = cleanedDescription.length();
                                    String summary = cleanedDescription.substring(0, length < 500 ? length : 500);
                                    article.put("summary", summary);
                                } catch (JSONException e) {
                                    Log.e("ArticlesAdapter", "Cannot precompute article", e);
                                }

                                articleItems.add(article);
                            }
                        }
                    });
        }
    }
}
```