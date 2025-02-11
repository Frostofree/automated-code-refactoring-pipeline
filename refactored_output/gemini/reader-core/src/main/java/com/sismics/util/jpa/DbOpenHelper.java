```java
import javax.sql.DataSource;

public abstract class DbOpenHelper {
    private final DataSource dataSource;

    public DbOpenHelper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void open() {
        var migrationManager = createMigrationManager();
        try (var connection = dataSource.getConnection()) {
            migrationManager.migrate(connection);
        } catch (Exception e) {
            migrationManager.addException(e);
        }
    }

    protected abstract MigrationManager createMigrationManager();

    interface MigrationManager {
        void migrate(Connection connection) throws Exception;
        void addException(Exception e);
    }
}

public class DatabaseMigrationManager implements MigrationManager {
    private final int currentVersion;
    private final List<Exception> exceptions = new ArrayList<>();

    public DatabaseMigrationManager(int currentVersion) {
        this.currentVersion = currentVersion;
    }

    @Override
    public void migrate(Connection connection) throws Exception {
        for (int i = currentVersion + 1; i <= getLatestVersion(); i++) {
            executeScript(connection, "migration_v" + i + ".sql");
        }
    }

    @Override
    public void addException(Exception e) {
        exceptions.add(e);
    }

    protected abstract int getLatestVersion();
    protected abstract void executeScript(Connection connection, String scriptName);
}
```