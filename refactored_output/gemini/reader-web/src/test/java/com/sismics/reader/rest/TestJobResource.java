```java
package com.sismics.reader.rest;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.MediaType;
import java.io.BufferedInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Exhaustive test of the job resource.
 *
 * @author jtremeaux
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JobResource.class, UserResource.class})
public class TestJobResource extends BaseJerseyTest {

    /**
     * Test of the job resource.
     *
     */
    @Test
    public void testJobResource() throws Exception {
        // Create user job1
        createUser("job1");

        // Create user job2
        createUser("job2");

        // Import opml
        importOpml("job1", "/import/greader_subscriptions.xml");

        // Check the user's job
        JobResourceTestUtil.checkJob("job1");

        // User job2 deletes user1's job KO : forbidden
        JobResourceTestUtil.deleteUserJob("job2");

        // User job1 deletes his job
        JobResourceTestUtil.deleteUserJob("job1");

        // Check that the job was deleted
        JobResourceTestUtil.checkDeletedJob();
    }

    private FormDataMultiPart getFormData(String user, String file) {
        return JobResourceTestUtil.getFormData(user, file);
    }
}

class JobResourceTestUtil {

    public static void checkJob(String user) throws Exception {
        UserResource userResource = new UserResource();
        User userObject = userResource.getCurrentUser();
        JSONArray jobs = userObject.getJobs();
        assertEquals(1, jobs.length());
        JSONObject job = (JSONObject) jobs.get(0);
        String jobId = job.getString("id");
        assertNotNull(jobId);
        assertEquals("import", job.optString("name"));
        assertNotNull(job.optString("start_date"));
        assertNotNull(job.optString("end_date"));
        assertEquals(4, job.optInt("feed_success"));
        assertEquals(0, job.optInt("feed_failure"));
        assertEquals(4, job.optInt("feed_total"));
        assertEquals(0, job.optInt("starred_success"));
        assertEquals(0, job.optInt("starred_failure"));
        assertEquals(0, job.optInt("starred_total"));
    }

    public static void deleteUserJob(String user) throws Exception {
        JobResource jobResource = new JobResource();
        JSONObject job = (JSONObject) jobResource.getCurrentUserJobs().get(0);
        String jobId = job.getString("id");
        jobResource.deleteJob(jobId);
    }

    public static void checkDeletedJob() throws Exception {
        UserResource userResource = new UserResource();
        User userObject = userResource.getCurrentUser();
        JSONArray jobs = userObject.getJobs();
        assertEquals(0, jobs.length());
    }

    public static FormDataMultiPart getFormData(String user, String file) {
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream track = TestJobResource.class.getResourceAsStream(file);
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(track),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        return form;
    }
}
```