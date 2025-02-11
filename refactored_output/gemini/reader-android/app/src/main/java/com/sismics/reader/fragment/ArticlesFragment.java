import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sismics.reader.R;
import com.sismics.reader.activity.MainActivity;
import com.sismics.reader.constant.Constants;
import com.sismics.reader.resource.ArticleResource;
import com.sismics.reader.resource.StarredResource;
import com.sismics.reader.util.DateUtils;
import com.sismics.reader.util.ImageUtils;
import com.sismics.reader.util.NetworkUtils;
import com.sismics.reader.util.UiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Articles adapter.
 */
public class ArticlesAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;

    private ArticlesFragment articlesFragment;  // Injected
    private LayoutInflater inflater;
    private List<JSONObject> articles;
    private SparseBooleanArray selectedPositions;
    private boolean selectionMode;
    private String subscriptionId;
    private boolean unread;

    public ArticlesAdapter(ArticlesFragment articlesFragment) {
        this.articlesFragment = articlesFragment;
        this.inflater = LayoutInflater.from(articlesFragment.getContext());
        this.articles = new ArrayList<>();
        this.selectedPositions = new SparseBooleanArray();
        this.selectionMode = false;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        notifyDataSetChanged();
    }

    public List<String> getSelectedArticles() {
        List<String> articleIds = new ArrayList<>();

        if (selectedPositions != null) {
            for (int i = 0; i < selectedPositions.size(); i++) {
                if (selectedPositions.valueAt(i)) {
                    articleIds.add(articles.get(selectedPositions.keyAt(i)).optString("id"));
                }
            }
        }

        return articleIds;
    }

    public void clearSelected() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void load(JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                articles.add(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();
    }

    public void removeArticle(int position) {
        articles.remove(position);
        notifyDataSetChanged();
    }

    public void notifyStarred(String articleId) {
        for (JSONObject article : articles) {
            if (articleId.equals(article.optString("id"))) {
                try {
                    if (unread) {
                        ArticleResource.unmarkAsUnread(articlesFragment.getContext(), subscriptionId, articleId);
                    }
                    article.put("starred", true);
                    article.put("read", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        notifyDataSetChanged();
    }

    public void notifyUnstarred(String articleId) {
        for (JSONObject article : articles) {
            if (articleId.equals(article.optString("id"))) {
                try {
                    article.put("starred", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return articles.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return articles.get(position);
    }

    @Override
    public long getItemId(int position) {
        try {
            return articles.get(position).getLong("id");
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (articles.get(position) == null) {
            return TYPE_LOADING;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        switch (getItemViewType(position)) {
            case TYPE_LOADING:
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.loading_row, parent, false);
                }
                return convertView;
            case TYPE_ITEM:
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.article_row, parent, false);
                    holder = new ViewHolder();
                    holder.articleCheckbox = convertView.findViewById(R.id.articleCheckbox);
                    holder.articleStarred = convertView.findViewById(R.id.articleStarred);
                    holder.articleTitle = convertView.findViewById(R.id.articleTitle);
                    holder.articleContent = convertView.findViewById(R.id.articleContent);
                    holder.articleRead = convertView.findViewById(R.id.articleRead);
                    holder.articleDate = convertView.findViewById(R.id.articleDate);