```java
package com.sismics.reader.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sismics.reader.fragment.ArticleFragment;

import org.json.JSONObject;

import java.util.List;

/**
 * Adapter for articles ViewPager.
 *
 * @author bgamard
 */
public class ArticlesPagerAdapter extends FragmentStatePagerAdapter {
    private List<JSONObject> mArticleItems;

    /**
     * Constructor.
     *
     * @param fm           FragmentManager
     * @param articleItems List of articles to display
     */
    public ArticlesPagerAdapter(FragmentManager fm, List<JSONObject> articleItems) {
        super(fm);
        mArticleItems = articleItems;
    }

    @Override
    public Fragment getItem(int position) {
        return ArticleFragment.newInstance(mArticleItems.get(position));
    }

    @Override
    public int getCount() {
        return mArticleItems.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_UNCHANGED;
    }
}
```