```java
package com.sismics.security;

import java.security.Principal;

/**
 * Interface of principals who have a profile associated to them.
 *
 * @author jtremeaux
 */
public interface IProfiledPrincipal extends Principal {
    /**
     * Returns the principal's profile.
     *
     * @return Profile of the principal
     */
    public IProfile getProfile();

    /**
     * Interface of a principal's profile.
     */
    public interface IProfile {
        /**
         * Returns the ID of the connected user.
         *
         * @return ID of the connected user
         */
        public String getId();
    }
}
```