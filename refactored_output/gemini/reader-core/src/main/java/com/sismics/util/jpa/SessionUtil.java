```java
package com.sismics.util.jpa.session;

import org.hibernate.Session;

/**
 * Interface for retrieving an instance of a Hibernate Session.
 */
public interface SessionProvider {

    /**
     * Returns an instance of a Hibernate Session.
     *
     * @return a valid Hibernate session
     */
    Session getSession();
}
```