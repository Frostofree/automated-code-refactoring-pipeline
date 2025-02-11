```java
package com.sismics.util.jpa.filter;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public abstract class FilterColumn {
    protected FilterColumn() {}

    public abstract String getPredicate();
    public abstract Object getParamValue();
    public abstract boolean hasParam();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterColumn that = (FilterColumn) o;

        return Objects.equal(getPredicate(), that.getPredicate())
                && Objects.equal(getParamValue(), that.getParamValue())
                && Objects.equal(hasParam(), that.hasParam());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPredicate(), getParamValue(), hasParam());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("predicate", getPredicate())
                .add("paramValue", getParamValue())
                .add("hasParam", hasParam())
                .toString();
    }
}
```