```java
package com.sismics.reader.rest.resource;

import com.sismics.reader.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.security.IPrincipal;
import com.sismics.security.SecurityContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * Base class of REST resources.
 *
 * @author jtremeaux
 */
public abstract class BaseResource {
    /**
     * Injects the HTTP request.
     */
    @Context
    protected HttpServletRequest request;

    /**
     * Application key.
     */
    @QueryParam("app_key")
    protected String appKey;

    /**
     * Principal of the authenticated user.
     */
    protected IPrincipal principal;

    /**
     * This method is used to initialize the security context.
     */
    protected void initSecurityContext() {
        SecurityContext.init(request);
    }

    /**
     * Checks if the user has a base function. Throw an exception if the check fails.
     *
     * @param baseFunction Base function to check
     */
    protected void checkBaseFunction(BaseFunction baseFunction) throws ForbiddenClientException {
        if (!UserPrincipal.hasBaseFunction(baseFunction)) {
            throw new ForbiddenClientException();
        }
    }
}
```