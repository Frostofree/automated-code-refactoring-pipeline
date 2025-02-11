```java
import com.sismics.reader.core.dao.jpa.CategoryDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.Category;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.exception.ClientException;
import com.sismics.reader.rest.exception.ForbiddenClientException;
import com.sismics.reader.rest.util.ValidationUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Category REST resources.
 *
 * @author jtremeaux
 */
@Path("/categories")
public class CategoryResource extends BaseResource {
    private CategoryDao categoryDao = new CategoryDao();
    private UserArticleDao userArticleDao = new UserArticleDao();

    /**
     * Returns all categories.
     *
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the root category
        Category rootCategory = categoryDao.getRootCategory(principal.getId());

        // Get the subcategories
        List<Category> categoryList = categoryDao.findSubCategory(rootCategory.getId(), principal.getId());

        // Build the response
        List<JSONObject> categoriesJson = new ArrayList<JSONObject>();
        for (Category category : categoryList) {
            JSONObject categoryJson = new JSONObject();
            categoryJson.put("id", category.getId());
            categoryJson.put("name", category.getName());
            categoriesJson.add(categoryJson);
        }

        JSONObject response = new JSONObject();
        response.put("categories", categoriesJson);
        return Response.ok().entity(response).build();
    }

    /**
     * Returns all articles in a category.
     *
     * @param id Category ID
     * @param unread Returns only unread articles
     * @param limit Page limit
     * @param afterArticle Start the list after this article
     * @return Response
     */
    @GET
    @Path("/{id: [a-z0-9\\-]+}/articles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("id") String id,
            @QueryParam("unread") boolean unread,
            @QueryParam("limit") Integer limit,
            @QueryParam("after_article") String afterArticle) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the category
        Category category;
        try {
            category = categoryDao.getCategory(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", id));
        }

        // Get the articles
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
                .setUnread(unread)
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setVisible(true);
        if (category.getParentId() != null) {
            userArticleCriteria.setCategoryId(id);
        }
        if (afterArticle != null) {
            // Paginate after this user article
            UserArticleCriteria afterArticleCriteria = new UserArticleCriteria()
                    .setUserArticleId(afterArticle)
                    .setUserId(principal.getId());
            List<UserArticleDto> userArticleDtoList = userArticleDao.findByCriteria(afterArticleCriteria);
            if (userArticleDtoList.isEmpty()) {
                throw new ClientException("ArticleNotFound", MessageFormat.format("Can't find user article {0}", afterArticle));
            }
            UserArticleDto userArticleDto = userArticleDtoList.iterator().next();

            userArticleCriteria.setArticlePublicationDateMax(new Date(userArticleDto.getArticlePublicationTimestamp()));
            userArticleCriteria.setArticleIdMax(userArticleDto.getArticleId());
        }

        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
        userArticleDao.findByCriteria(paginatedList, userArticleCriteria, null, null);

        // Build the JSON response
        JSONObject response = new JSONObject();
        List<JSONObject> articles = new ArrayList<JSONObject>();
        for (UserArticleDto userArticle : paginatedList.getResultList()) {
            JSONObject articleJson = new JSONObject();
            articleJson.put("id", userArticle.getUserArticleId());
            articleJson.put("title", userArticle.getArticleTitle());
            articleJson.put("content", userArticle.getArticleContent());
            articleJson.put("feed_id", userArticle.getFeedId());
            articleJson.put("feed_title", userArticle.getFeedTitle());
            articleJson.put("unread", userArticle.getUnread());
            articleJson.put("publication_date", userArticle.getArticlePublicationDate());
            articleJson.put("author", userArticle.getArticleAuthor());
            articles.add(articleJson);
        }
        response.put("articles", articles);

        return Response.ok().entity(response).build();
    }

    /**
     * Creates a new category.
     *
     * @param name Category name
     * @return Response
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("name") String name) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 100, false);

        // Get the root category
        Category rootCategory = categoryDao.getRootCategory(principal.getId());

        // Get the display order
        int displayOrder = categoryDao.getCategoryCount(rootCategory.getId(), principal.getId());

        // Create the category
        Category category = new Category();
        category.setUserId(principal.getId());
        category.setParentId(rootCategory.getId());
        category.setName(name);
        category.setOrder(displayOrder);
        categoryDao.create(category);

        JSONObject response = new JSONObject();
        response.put("id", category.getId());
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes a category.
     *
     * @param id Category ID
     * @return Response
     */
    @DELETE
    @Path("/{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the category
        try {
            categoryDao.getCategory(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", id));
        }

        // Move subscriptions in this category to root
        List<FeedSubscription> feedSubscriptionList = categoryDao.findByCategoryName(id);
        Category rootCategory = categoryDao.getRootCategory(principal.getId());
        for (FeedSubscription feedSubscription : feedSubscriptionList) {
            feedSubscription.setCategoryId(rootCategory.getId());
            categoryDao.update(feedSubscription);
            categoryDao.reorder(feedSubscription, 0);
        }

        // Delete
        categoryDao.delete(id);

        JSONObject response = new JSONObject();
        response.put("ok", true);
        return Response.ok().entity(response).build();
    }
}
```