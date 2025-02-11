```java
package com.sismics.reader.rest.assembler;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;

public class ArticleAssembler {

    public static JSONObject asJson(UserArticleDto userArticle) throws JSONException {
        return userArticle.asJson();
    }
}

package com.sismics.reader.core.dao.jpa.dto;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.reader.rest.assembler.SubscriptionJsonAssembler;

public class UserArticleDto {

    private Long id;
    private Long feedSubscriptionId;
    private String feedSubscriptionTitle;
    private String feedTitle;
    private Long articleId;
    private String articleTitle;
    private String articleUrl;
    private Long articlePublicationTimestamp;
    private String articleCreator;
    private String articleDescription;
    private String articleCommentUrl;
    private Integer articleCommentCount;
    private String articleEnclosureUrl;
    private Long articleEnclosureLength;
    private String articleEnclosureType;
    private Long readTimestamp;
    private Long starTimestamp;

    public JSONObject asJson() throws JSONException {
        JSONObject userArticleJson = new JSONObject();
        userArticleJson.put("id", id);
        JSONObject subscriptionJson = new SubscriptionJsonAssembler().assemble(this);
        userArticleJson.put("subscription", subscriptionJson);
        userArticleJson.put("article", asArticleJson());
        userArticleJson.put("is_read", readTimestamp != null);
        userArticleJson.put("is_starred", starTimestamp != null);
        return userArticleJson;
    }

    private JSONObject asArticleJson() throws JSONException {
        JSONObject articleJson = new JSONObject();
        articleJson.put("title", articleTitle);
        articleJson.put("url", articleUrl);
        articleJson.put("date", articlePublicationTimestamp);
        articleJson.put("creator", articleCreator);
        articleJson.put("description", articleDescription);
        articleJson.put("comment_url", articleCommentUrl);
        articleJson.put("comment_count", articleCommentCount);
        if (articleEnclosureUrl != null) {
            JSONObject enclosure = new JSONObject();
            enclosure.put("url", articleEnclosureUrl);
            enclosure.put("length", articleEnclosureLength);
            enclosure.put("type", articleEnclosureType);
            articleJson.put("enclosure", enclosure);
        }
        return articleJson;
    }

    // getters and setters
}
```