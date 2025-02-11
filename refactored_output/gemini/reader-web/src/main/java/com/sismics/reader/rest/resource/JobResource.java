```java
package com.sismics.reader.rest.resource;

import com.sismics.reader.core.model.jpa.Job;
import com.sismics.reader.core.service.JobService;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Job REST resources.
 *
 * @author jtremeaux
 */
@Path("/job")
public class JobResource extends BaseResource {
    /**
     * Deletes a job.
     *
     * @param id Job ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Delegate
        JobService jobService = new JobService(principal.getId());
        try {
            jobService.deleteJob(new Job(id));
            
            // Always return ok
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            return Response.ok().entity(response).build();
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException("FailedToDeleteJob", "Failed to delete job", e);
        }
    }
}
```