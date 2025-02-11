```java
package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_CATEGORY")
public class Category {

    @Id
    private String id;
    private String userId;
    private String parentId;
    private String name;
    private Integer order;
    private boolean folded;
    private Date createDate;
    private Date deleteDate;

    // getters and setters
}
```