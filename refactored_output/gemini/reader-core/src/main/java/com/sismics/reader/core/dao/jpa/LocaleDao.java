```java
package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.model.jpa.Locale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocaleDao extends JpaRepository<Locale, String> {
}
```