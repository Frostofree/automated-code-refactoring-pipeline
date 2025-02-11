```java
package com.sismics.util.jpa.filter;

import java.util.List;

/**
 * Specification for filtering a query.
 *
 * @author jtremeaux
 */
public interface FilterSpecification<E> {
    boolean isSatisfiedBy(E entity);

    static <E> FilterSpecification<E> fromColumns(List<FilterColumn> filterColumns) {
        // Implementation of the specification logic
        // ...
    }
}
```