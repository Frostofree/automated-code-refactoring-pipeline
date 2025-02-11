```java
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Articles REST resource.
 *
 * @author jtremeaux
 */
@Path("/articles")
public class ArticleResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArticles(
        @QueryParam("unread") boolean unread,
        @QueryParam("limit") Integer limit,
        @QueryParam("after_article") String afterArticle
    ) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
            .setUnread(unread)
            .setUserId(principal.getId())
            .setSubscribed(true)
            .setVisible(true);
        addPagination(userArticleCriteria, afterArticle, limit);

        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
        UserArticleDao userArticleDao = new UserArticleDao();
        userArticleDao.findByCriteria(paginatedList, userArticleCriteria, null, null);

        List<JSONObject> articles = new ArrayList<>();
        for (UserArticleDto userArticle : paginatedList.getResultList()) {
            articles.add(ArticleAssembler.asJson(userArticle));
        }
        JSONObject response = new JSONObject();
        response.put("articles", articles);
        return Response.ok().entity(response).build();
    }

    private void addPagination(UserArticleCriteria userArticleCriteria, String afterArticle, Integer limit) {
        if (afterArticle != null) {
            // Paginate after this user article
            UserArticleCriteria afterArticleCriteria = new UserArticleCriteria()
                .setUserArticleId(afterArticle)
                .setUserId(principal.getId());
            List<UserArticleDto> userArticleDtoList = new UserArticleDao().findByCriteria(afterArticleCriteria);
            if (userArticleDtoList.isEmpty()) {
                throw new ClientException("ArticleNotFound", MessageFormat.format("Can't find user article {0}", afterArticle));
            }
            UserArticleDto userArticleDto = userArticleDtoList.iterator().next();

            userArticleCriteria.setArticlePublicationDateMax(new Date(userArticleDto.getArticlePublicationTimestamp()));
            userArticleCriteria.setArticleIdMax(userArticleDto.getArticleId());
        }
        PaginatedList.applyLimit(limit, userArticleCriteria);
    }

    @POST
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response readArticles() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        UserArticleCriteria criteria = new UserArticleCriteria()
            .setUserId(principal.getId())
            .setUnread(true);
        new UserArticleDao().markAsRead(criteria);

        return Response.ok().entity(new JSONObject().put("status", "ok")).build();
    }
}
```