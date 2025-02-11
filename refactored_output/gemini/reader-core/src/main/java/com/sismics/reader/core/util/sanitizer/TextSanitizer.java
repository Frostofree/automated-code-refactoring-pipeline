```java
package com.sismics.reader.core.util.sanitizer;

import java.util.regex.Pattern;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Sanitizes HTML text by removing all elements.
 *
 * @author jtremeaux 
 */
public class HtmlSanitizer {
    private final static PolicyFactory policy = new HtmlPolicyBuilder().toFactory();

    public static String sanitize(String html) {
        return policy.sanitize(html);
    }
}
```

```java
package com.sismics.reader.core.util.sanitizer;

import java.util.regex.Pattern;

/**
 * Converts HTML entities to Unicode.
 *
 * @author jtremeaux 
 */
public class HtmlEntityConverter {
    private final static Pattern TAG_PATTERN = Pattern.compile("&lt;.+&gt;");

    public static String convert(String html) {
        return TAG_PATTERN.matcher(html).replaceAll("");
    }
}
```