```java
package com.sismics.reader.core.dao;

import com.sismics.reader.core.model.User;
import com.sismics.reader.core.dao.criteria.UserCriteria;

public interface UserDao {

    String create(User user);

    User getById(String id);

    // Move authentication methods to a separate interface
    interface AuthenticationDao {
        User getActiveByUsername(String username);
        User getActiveByPasswordResetKey(String passwordResetKey);
    }

    // Move profile update methods to a separate interface
    interface ProfileDao {
        void updateProfile(User user, String localeId, String email, String theme);
    }

    // Move deletion methods to a separate interface
    interface DeletionDao {
        void delete(String username);
    }
}
```