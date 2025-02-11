```java
package com.sismics.reader.rest.resource;

import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.reader.search.service.SearchService;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Search articles REST resources.
 *
 * @author jtremeaux
 */
@Path("/search")
public class SearchResource extends BaseResource {

    /**
     * Returns articles matching a search query.
     *
     * @param query Search query
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     */
    @GET
    @Path("{query: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("query") String query,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(query, "query");

        // Search in index
        SearchService searchService = AppContext.getInstance().getSearchService();
        try {
            return Response.ok().entity(searchService.search(query, limit, offset)).build();
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching articles", e);
        }
    }
}
```