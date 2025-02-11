```java
import com.sismics.reader.core.util.IndexingService;
import com.sismics.reader.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * General app REST resource.
 *
 * @author jtremeaux
 */
@Path("/app")
public class AppResource extends BaseResource {

    private final IndexingService indexingService;

    public AppResource(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    /**
     * Return the information about the application.
     *
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response version() throws JSONException {
        JSONObject response = new JSONObject();
        response.put("current_version", "1.0.0");
        response.put("min_version", "0.9.0");
        response.put("total_memory", Runtime.getRuntime().totalMemory());
        response.put("free_memory", Runtime.getRuntime().freeMemory());
        return Response.ok().entity(response).build();
    }

    /**
     * Perform authenticated action.
     *
     * @param request HTTP request
     * @param method HTTP method
     * @return JSONAPI response
     */
    private Response authenticatedAction(HttpMethod method, String requestPath) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        Response response;
        try {
            switch (method) {
                case POST:
                    switch (requestPath) {
                        case "batch/reindex":
                            indexingService.rebuildIndex();
                            response = Response.ok(new JSONObject().put("status", "ok")).build();
                            break;
                        default:
                            throw new ServerException("InternalError", "Unknown authenticated POST action: " + requestPath);
                    }
                    break;
                default:
                    throw new ServerException("InternalError", "Unknown authenticated HTTP method: " + method);
            }
        } catch (Exception e) {
            throw new ServerException("InternalError", e.getMessage(), e);
        }

        return response;
    }

    @POST
    @Path("/map_port")
    public Response mapPort() throws JSONException {
        Response response;
        try {
            if (!NetworkUtil.mapTcpPort(request.getServerPort())) {
                throw new ServerException("NetworkError", "Error mapping port using UPnP");
            }
            response = Response.ok(new JSONObject().put("status", "ok")).build();
        } catch (Exception e) {
            throw new ServerException("InternalError", e.getMessage(), e);
        }
        return response;
    }
}
```