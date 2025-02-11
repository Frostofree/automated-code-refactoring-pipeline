```java
package com.sismics.rest.resource;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public javax.ws.rs.core.Response toResponse(Exception e) {
        return ExceptionMapperUtils.toResponse(e, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
    }
}
```