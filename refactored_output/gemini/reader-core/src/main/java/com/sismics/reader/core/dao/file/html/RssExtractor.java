```java
package com.sismics.reader.core.dao.file.html;

import com.sismics.util.UrlUtil;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * HTML parser used to look for RSS / Atom feeds.
 *
 * @author jtremeaux
 */
public class RssExtractor {
    private final String url;

    public RssExtractor(String url) {
        this.url = url;
    }

    public List<String> extractFeeds(InputStream is) throws Exception {
        return new FeedExtractor(UrlUtil.toURL(url)).parse(is);
    }

    private static class FeedExtractor {
        private final List<String> feedList = new ArrayList<>();

        public FeedExtractor(URL url) {
            this.url = url;
        }

        public List<String> parse(InputStream is) throws Exception {
            HtmlParser parser = new HtmlParser() {
                @Override
                protected void onLink(String rel, String type, String href) {
                    addFeed(rel, type, href);
                }
            };
            parser.parse(is);
            return feedList;
        }

        private void addFeed(String rel, String type, String href) {
            if ("alternate".equalsIgnoreCase(rel)) {
                try {
                    if ("application/rss+xml".equalsIgnoreCase(type)) {
                        feedList.add(UrlUtil.completeUrl(url.toString(), href));
                    } else if ("application/atom+xml".equalsIgnoreCase(type)) {
                        feedList.add(UrlUtil.completeUrl(url.toString(), href));
                    }
                } catch (MalformedURLException e) {
                    // TODO: Handle exception
                }
            }
        }
    }

    private static abstract class HtmlParser {
        protected void parse(InputStream is) throws Exception {}
        protected abstract void onLink(String rel, String type, String href);
    }
}
```