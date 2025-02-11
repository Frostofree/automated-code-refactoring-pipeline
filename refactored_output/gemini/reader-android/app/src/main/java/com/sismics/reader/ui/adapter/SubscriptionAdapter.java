```java
package com.sismics.reader.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.reader.R;
import com.sismics.reader.model.Subscription;
import com.sismics.reader.model.SubscriptionCategory;
import com.sismics.reader.util.PreferenceUtil;

/**
 * Adapter for subscriptions list.
 *
 * @author bgamard
 */
public class SubscriptionAdapter extends BaseAdapter {

    private List<Subscription> items;
    private Context context;
    private AQuery aq;

    public SubscriptionAdapter(Context context, JSONObject input) {
        this.context = context;
        this.aq = new AQuery(context);
        setItems(input);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Subscription item = getItem(position);

        int layout;
        switch (item.getType()) {
            case Subscription.HEADER_ITEM:
                layout = R.layout.drawer_list_item_header;
                break;
            case Subscription.SUBSCRIPTION_ITEM:
                layout = R.layout.drawer_list_item_subscription;
                break;
            case Subscription.CATEGORY_ITEM:
                layout = R.layout.drawer_list_item_category;
                break;
            default:
                throw new IllegalStateException("Unknown item type: " + item.getType());
        }

        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(layout, null);
        }

        aq.recycle(view);

        switch (item.getType()) {
            case Subscription.SUBSCRIPTION_ITEM:
                if (item.getId() != null) {
                    String faviconUrl = PreferenceUtil.getServerUrl(context) + "/api/subscription/" + item.getId() + "/favicon";
                    Bitmap placeHolder = aq.getCachedImage(R.drawable.ic_launcher);
                    aq.id(R.id.imgFavicon)
                            .image(new BitmapAjaxCallback()
                                    .url(faviconUrl)
                                    .fallback(R.drawable.ic_launcher)
                                    .preset(placeHolder)
                                    .animation(AQuery.FADE_IN_NETWORK)
                            )
                            .margin(item.isRoot() ? 16 : 32, 0, 0, 0);
                } else {
                    int imgRes;
                    if (item.getUrl().equals("/all")) {
                        imgRes = item.isUnread() ? R.drawable.drawer_list_item_unread : R.drawable.drawer_list_item_read;
                    } else if (item.getUrl().equals("/starred")) {
                        imgRes = R.drawable.drawer_list_item_important;
                    } else {
                        imgRes = 0;
                    }
                    if (imgRes != 0) aq.id(R.id.imgFavicon).image(imgRes);
                }
                break;
        }

        aq.id(R.id.content).text(item.getTitle());
        if (item.getUnreadCount() == 0) {
            aq.id(R.id.unreadCount).gone();
        } else {
            aq.id(R.id.unreadCount).visible().text("" + item.getUnreadCount());
        }

        return view;
    }

    private Subscription getItem(int position) {
        try {
            return items.get(position);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private List<Subscription> parseSubscriptions(JSONObject input) {
        List<Subscription> items = new ArrayList<Subscription>();

        // Adding fixed items
        SubscriptionItem item1 = new SubscriptionItem();
        item1.setType(Subscription.HEADER_ITEM);
        item1.setTitle(context.getString(R.string.latest));
        items.add(item1);

        SubscriptionItem item2 = new SubscriptionItem();
        item2.setType(Subscription.SUBSCRIPTION_ITEM);
        item2.setTitle(context.getString(R.string.unread));
        item2.setUrl("/all");
        item2.setUnread(true);
        item2.setUnreadCount(input.optInt("unread_count"));
        items.add(item2);

        SubscriptionItem item3 = new SubscriptionItem();
        item3.setType(Subscription.SUBSCRIPTION_ITEM);
        item3.setTitle(context.getString(R.string.all));
        item3.setUrl("/all");
        item3.setUnread(false);
        items.add(item3);

        SubscriptionItem item4 = new SubscriptionItem();
        item4.setType(Subscription.SUBSCRIPTION_ITEM);
        item4.setTitle(context.getString(R.string.starred));
        item4.setUrl("/starred");
        items.add(item4);

        SubscriptionItem item5 = new SubscriptionItem();
        item5.setType(Subscription.HEADER_ITEM);
        item5.setTitle(context.getString(R.string.subscriptions));
        items.add(item5);

        // Adding categories and subscriptions
        JSONObject rootCategory = input.optJSONArray("categories").optJSONObject(0);
        JSONArray categories = rootCategory.optJSONArray("categories");
        if (categories != null) {
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.optJSONObject(i);

                SubscriptionCategory categoryItem = new SubscriptionCategory();
                categoryItem.setType(Subscription.CATEGORY_ITEM);
                categoryItem.setId(category.optString("id"));
                categoryItem.setTitle(category.optString("name"));
                categoryItem.setUrl("/category/" + categoryItem.getId());
                categoryItem.setUnreadCount(category.optInt("unread_count"));
                items.add(categoryItem);

                JSONArray subscriptions = category.optJSONArray("subscriptions");
                if (subscriptions != null) {
                    for (int j = 0; j < subscriptions.length(); j++) {
                        JSONObject subscription = subscriptions.optJSONObject(j);

                        Subscription subscriptionItem = new Subscription();
                        subscriptionItem.setType(Subscription.SUBSCRIPTION_ITEM);
                        subscriptionItem.setId(subscription.optString("id"));
                        subscriptionItem.setTitle(subscription.optString("title"));
                        subscriptionItem.setUrl("/subscription/" + subscriptionItem.getId());
                        subscriptionItem.setUnreadCount(subscription.optInt("unread_count"));
                        items.add(subscriptionItem);
                    }
                }
            }
        }

        // Root subscriptions
        JSONArray subscriptions = rootCategory.optJSONArray("subscriptions");
        if (subscriptions != null) {
            for (int j = 0; j < subscriptions.length(); j++) {
                JSONObject subscription = subscriptions.optJSONObject(j);

                Subscription subscriptionItem = new Subscription();
                subscriptionItem.setType(Subscription.SUBSCRIPTION_ITEM);
                subscriptionItem.setId(subscription.optString("id"));
                subscriptionItem.setTitle(subscription.optString("title"));
                subscriptionItem.setUrl("/subscription/" + subscriptionItem.getId());
                subscriptionItem.setUnreadCount(subscription.optInt("unread_count"));
                subscriptionItem.setRoot(true);
                items.add(subscriptionItem);
            }
        }

        return items;
    }

    public void setItems(JSONObject input) {
        items = parseSubscriptions(input);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        Subscription item = getItem(position);
        return item != null
                && (item.getType() == Subscription.SUBSCRIPTION_ITEM
                || item.getType() == Subscription.CATEGORY_ITEM);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    private static class SubscriptionItem extends Subscription {
        private boolean isUnread;

        public boolean isUnread() {
            return isUnread;
        }

        public void setUnread(boolean unread) {
            this.isUnread = unread;
        }
    }
}
```