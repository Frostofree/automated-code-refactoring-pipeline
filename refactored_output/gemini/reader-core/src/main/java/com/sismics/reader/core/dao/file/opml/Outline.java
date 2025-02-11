```java
package com.sismics.reader.core.dao.file.opml.model;

/**
 * OPML outline information.
 */
public class OutlineInfo {
    private String text;
    private String title;
    private String type;

    public OutlineInfo(String text, String title, String type) {
        this.text = text;
        this.title = title;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }
}

/**
 * OPML outline URLs.
 */
public class OutlineUrl {
    private String xmlUrl;
    private String htmlUrl;

    public OutlineUrl(String xmlUrl, String htmlUrl) {
        this.xmlUrl = xmlUrl;
        this.htmlUrl = htmlUrl;
    }

    public String getXmlUrl() {
        return xmlUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }
}
```