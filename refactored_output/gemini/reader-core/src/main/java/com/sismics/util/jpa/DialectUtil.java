**Database-Related Operations:**
```java
public interface DatabaseUtil {

    boolean isObjectNotFound(String message);

    String getNullParameter(String parameterName, Object value);

}
```

**String Manipulations:**
```java
public interface StringUtil {

    String transformToPostgresql(String sql);

}
```

**Date Calculations:**
```java
public interface DateUtil {

    String getDateDiff(String field, String diff, String unit);

    String getTimeStamp(String value);

}
```

**Dialect Interface:**
```java
public interface Dialect {

    DatabaseUtil getDatabaseUtil();

    StringUtil getStringUtil();

    DateUtil getDateUtil();

}
```

**Dialect Factory:**
```java
public class DialectFactory {

    public static Dialect getDialect(PersistenceContext context) {
        switch (context.getDialectType()) {
            case "hsql":
                return new HsqlDialect(new HsqlDatabaseUtil(), new HsqlStringUtil(), new HsqlDateUtil());
            case "postgresql":
                return new PostgresqlDialect(new PostgresqlDatabaseUtil(), new PostgresqlStringUtil(), new PostgresqlDateUtil());
            default:
                throw new RuntimeException("Unknown DB: " + context.getDialectType());
        }
    }

}
```

**HsqlDialect:**
```java
public class HsqlDialect implements Dialect {

    private final DatabaseUtil databaseUtil;
    private final StringUtil stringUtil;
    private final DateUtil dateUtil;

    public HsqlDialect(DatabaseUtil databaseUtil, StringUtil stringUtil, DateUtil dateUtil) {
        this.databaseUtil = databaseUtil;
        this.stringUtil = stringUtil;
        this.dateUtil = dateUtil;
    }

    @Override
    public DatabaseUtil getDatabaseUtil() {
        return databaseUtil;
    }

    @Override
    public StringUtil getStringUtil() {
        return stringUtil;
    }

    @Override
    public DateUtil getDateUtil() {
        return dateUtil;
    }
}
```

**PostgresqlDialect:**
```java
public class PostgresqlDialect implements Dialect {

    private final DatabaseUtil databaseUtil;
    private final StringUtil stringUtil;
    private final DateUtil dateUtil;

    public PostgresqlDialect(DatabaseUtil databaseUtil, StringUtil stringUtil, DateUtil dateUtil) {
        this.databaseUtil = databaseUtil;
        this.stringUtil = stringUtil;
        this.dateUtil = dateUtil;
    }

    @Override
    public DatabaseUtil getDatabaseUtil() {
        return databaseUtil;
    }

    @Override
    public StringUtil getStringUtil() {
        return stringUtil;
    }

    @Override
    public DateUtil getDateUtil() {
        return dateUtil;
    }
}
```

**HsqlDatabaseUtil:**
```java
public class HsqlDatabaseUtil implements DatabaseUtil {

    @Override
    public boolean isObjectNotFound(String message) {
        return message.contains("object not found");
    }

    @Override
    public String getNullParameter(String parameterName, Object value) {
        if (value == null) {
            return "null";
        } else {
            return parameterName;
        }
    }

}
```

**PostgresqlDatabaseUtil:**
```java
public class PostgresqlDatabaseUtil implements DatabaseUtil {

    @Override
    public boolean isObjectNotFound(String message) {
        return message.contains("does not exist");
    }

    @Override
    public String getNullParameter(String parameterName, Object value) {
        if (value == null) {
            return "null";
        } else {
            return parameterName;
        }
    }

}
```

**HsqlStringUtil:**
```java
public class HsqlStringUtil implements StringUtil {

    @Override
    public String transformToPostgresql(String sql) {
        return sql.replaceAll("(cached|memory) table", "table");
    }

}
```

**PostgresqlStringUtil:**
```java
public class PostgresqlStringUtil implements StringUtil {

    @Override
    public String transformToPostgresql(String sql) {
        return sql.replaceAll("datetime", "timestamp")
                .replaceAll("longvarchar", "text")
                .replaceAll("bit not null", "bool not null")
                .replaceAll("bit default 0", "bool default false");
    }

}
```

**HsqlDateUtil:**
```java
public class HsqlDateUtil implements DateUtil {

    @Override
    public String getDateDiff(String field, String diff, String unit) {
        return "DATE_SUB(" + field + ", INTERVAL " + diff + " " + unit + ")";
    }

    @Override
    public String getTimeStamp(String value) {
        return "TIMESTAMP(" + value + ")";
    }

}
```

**PostgresqlDateUtil:**
```java
public class PostgresqlDateUtil implements DateUtil {

    @Override
    public String getDateDiff(String field, String diff, String unit) {
        return field + " - (" + diff + " * interval '1 " + unit + "')";
    }

    @Override
    public String getTimeStamp(String value) {
        return "to_char(" + value + " at time zone 'UTC', 'YYYY-MM-DD HH24:MI:SS.MS')";
    }

}
```