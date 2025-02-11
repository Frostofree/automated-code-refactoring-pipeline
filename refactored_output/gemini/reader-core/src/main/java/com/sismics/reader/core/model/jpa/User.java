```java
package com.sismics.reader.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_USER")
public class UserEntity {
    @Id
    @Column(name = "USE_ID_C", length = 36)
    private String id;
    @Column(name = "USE_USERNAME_C", nullable = false, length = 50)
    private String username;
    @Column(name = "USE_PASSWORD_C", nullable = false, length = 100)
    private String password;
    @Column(name = "USE_EMAIL_C", nullable = false, length = 100)
    private String email;

    public UserEntity() {}

    public UserEntity(String id, String username, String password, String email) {
        super();
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, email);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        UserEntity that = (UserEntity) other;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(email, that.email);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("username", username).toString();
    }
}

@Entity
@Table(name = "T_USER_DATE")
public class UserDateEntity {
    @Id
    private String id;
    private Date createDate;

    public UserDateEntity() {}

    public UserDateEntity(String id, Date createDate) {
        super();
        this.id = id;
        this.createDate = createDate;
    }

    public String getId() {
        return id;
    }

    public Date getCreateDate() {
        return createDate;
    }
}
```