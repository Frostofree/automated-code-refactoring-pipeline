package com.sismics.reader.rest.resource;

import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.service.ArticleService;
import com.sismics.reader.core.service.dto.ArticleDto;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.reader.rest.resource.base.BaseResource;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
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
 * Starred articles REST resources.
 */
@Path("/starred")
public class StarredResource extends BaseResource {
    /**
     * Returns starred articles.
     *
     * @param limit Page limit
     * @param afterArticle Start the list after this article
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("limit") Integer limit,
            @QueryParam("after_article") String afterArticle) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the articles
        ArticleService articleService = new ArticleService();
        PaginatedList<ArticleDto> paginatedList = PaginatedLists.create(limit, null);
        articleService.findForUser(paginatedList, principal.getId(), true, true, afterArticle);

        // Build the response
        JSONObject response = new JSONObject();

        List<JSONObject> articles = new ArrayList<JSONObject>();
        for (ArticleDto article : paginatedList.getResultList()) {
            articles.add(ArticleAssembler.asJson(article));
        }
        response.put("articles", articles);

        return Response.ok().entity(response).build();
    }
}