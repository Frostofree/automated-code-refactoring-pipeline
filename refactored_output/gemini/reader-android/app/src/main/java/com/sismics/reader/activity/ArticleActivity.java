```java
public class ArticleActivity extends AppCompatActivity {

    private ArticlesPagerAdapter articlesPagerAdapter;
    private ArticlesAdapter articlesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        // Initialize viewPager and set its adapter
        ViewPager viewPager = findViewById(R.id.viewPager);
        articlesPagerAdapter = new ArticlesPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(articlesPagerAdapter);

        // Get the ArticlesAdapter from the ArticlesPagerAdapter
        articlesAdapter = articlesPagerAdapter.getArticlesAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.article_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.share_article:
                // Handle article sharing
                return true;
            case R.id.star_article:
                // Handle article starring
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Other class methods...
}
```

```java
public class ArticlesPagerAdapter extends FragmentPagerAdapter {

    private ArticlesAdapter articlesAdapter;

    public ArticlesPagerAdapter(FragmentManager fm) {
        super(fm);
        articlesAdapter = new ArticlesAdapter();
    }

    @Override
    public Fragment getItem(int position) {
        return articlesAdapter.getItem(position);
    }

    @Override
    public int getCount() {
        return articlesAdapter.getCount();
    }

    public ArticlesAdapter getArticlesAdapter() {
        return articlesAdapter;
    }
}
```

```java
public class ArticlesAdapter extends FragmentStatePagerAdapter {

    // Adapter implementation...
}
```