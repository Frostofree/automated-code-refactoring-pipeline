```java
package com.sismics.reader.core.util.jpa;

public class SortCriteria {

    private final int column;
    private final boolean asc;

    public SortCriteria(int column, boolean asc) {
        this.column = column;
        this.asc = asc;
    }

    public int getColumn() {
        return column;
    }

    public boolean isAsc() {
        return asc;
    }
}

package com.sismics.reader.core.util.jpa;

public class SortQueryBuilder {

    private final StringBuilder builder = new StringBuilder();

    public SortQueryBuilder add(SortCriteria criteria) {
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(criteria.getColumn()).append(" ").append(criteria.isAsc() ? "ASC" : "DESC");
        return this;
    }

    public String build() {
        return builder.toString();
    }
}
```