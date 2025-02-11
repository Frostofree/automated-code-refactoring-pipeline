```java
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.sismics.util.adblock.JSEngine;
import com.sismics.util.adblock.Subscription;
import com.sismics.util.adblock.SubscriptionParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdblockUtil {

    private static final Logger log = LoggerFactory.getLogger(AdblockUtil.class);
    private static final JSEngine jsEngine = new JSEngine();

    private static List<Subscription> subscriptions;

    static {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(AdblockUtil.class.getResourceAsStream("/adblock/subscriptions.xml"), new SubscriptionParser(subscriptions));
        } catch (Exception e) {
            log.error("Error parsing subscriptions", e);
        }
    }

    public static List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public static Subscription getSubscription(String url) {
        for (Subscription subscription : subscriptions) {
            if (subscription.url.equals(url))
                return subscription;
        }
        return null;
    }

    public static void setSubscription(Subscription subscription) throws Exception {
        if (subscription != null) {
            final JSONObject jsonSub = new JSONObject();
            jsonSub.put("url", subscription.url);
            jsonSub.put("title", subscription.title);
            jsonSub.put("homepage", subscription.homepage);
            jsEngine.evaluate("clearSubscriptions()");
            jsEngine.evaluate("addSubscription(\"" + StringEscapeUtils.escapeJavaScript(jsonSub.toString()) + "\")");
        }
    }

    public static void refreshSubscription() throws ScriptException {
        jsEngine.evaluate("refreshSubscriptions()");
    }

    public static Subscription offerSubscription() {
        Subscription selectedItem = null;
        String selectedPrefix = null;
        int matchCount = 0;
        for (Subscription subscription : subscriptions) {
            if (selectedItem == null)
                selectedItem = subscription;

            String prefix = checkLocalePrefixMatch(subscription.prefixes);
            if (prefix != null) {
                if (selectedPrefix == null || selectedPrefix.length() < prefix.length()) {
                    selectedItem = subscription;
                    selectedPrefix = prefix;
                    matchCount = 1;
                } else if (selectedPrefix != null && selectedPrefix.length() == prefix.length()) {
                    matchCount++;

                    // If multiple items have a matching prefix of the
                    // same length select one of the items randomly,
                    // probability should be the same for all items.
                    // So we replace the previous match here with
                    // probability 1/N (N being the number of matches).
                    if (Math.random() * matchCount < 1) {
                        selectedItem = subscription;
                        selectedPrefix = prefix;
                    }
                }
            }
        }
        return selectedItem;
    }

    public static boolean verifySubscriptions() throws ScriptException {
        return (Boolean) jsEngine.evaluate("verifySubscriptions()");
    }

    public static boolean matches(String url, String query, String reqHost, String refHost, String accept) throws Exception {
        return (Boolean) jsEngine.evaluate("matchesAny('"
                + StringEscapeUtils.escapeJavaScript(url) + "', '"
                + StringEscapeUtils.escapeJavaScript(query) + "', '"
                + (reqHost != null ? StringEscapeUtils.escapeJavaScript(reqHost) : "") + "', '"
                + (refHost != null ? StringEscapeUtils.escapeJavaScript(refHost) : "") + "', '"
                + (accept != null ? StringEscapeUtils.escapeJavaScript(accept) : "") + "');");
    }

    public static String checkLocalePrefixMatch(String[] prefixes) {
        if (prefixes == null || prefixes.length == 0)
            return null;

        String locale = Locale.getDefault().toString().toLowerCase();

        for (int i = 0; i < prefixes.length; i++)
            if (locale.startsWith(prefixes[i].toLowerCase()))
                return prefixes[i];

        return null;
    }

    public static void start() throws Exception {
        URL url = Resources.getResource("adblock" + File.separator + "js" + File.separator + "start.js");
        jsEngine.put("_locale", Locale.getDefault().toString());
        jsEngine.put("_datapath", "");
        jsEngine.put("_separator", File.separator);
        jsEngine.put("_version", "");
        jsEngine.put("Android", new Helper(jsEngine));
        jsEngine.evaluate(Resources.toString(url, Charsets.UTF_8));
    }
}
```