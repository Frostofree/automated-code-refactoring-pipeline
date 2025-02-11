```java
package com.sismics.util.context;

import com.sismics.reader.core.model.context.AppContext;

import javax.servlet.*;
import java.io.IOException;

/**
 * Filter used to initialize the request context.
 *
 * @author jtremeaux
 */
public class RequestContextFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // NOP
    }

    @Override
    public void destroy() {
        // NOP
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        new RequestContext(filterChain, request).execute();
    }

    private static class RequestContext implements AutoCloseable {
        private FilterChain filterChain;
        private ServletRequest request;

        public RequestContext(FilterChain filterChain, ServletRequest request) {
            this.filterChain = filterChain;
            this.request = request;

            initContext();
        }

        private void initContext() {
            // Initialize request context
            ThreadLocalContext.get().init(AppContext.fromRequest(request));
        }

        public void execute() throws IOException, ServletException {
            try {
                filterChain.doFilter(request, ResponseWrapper.wrap(request, response));
            } finally {
                cleanupContext();
            }
        }

        private void cleanupContext() {
            // Cleanup request context
            ThreadLocalContext.cleanup();
        }

        @Override
        public void close() {
            cleanupContext();
        }
    }
}
```