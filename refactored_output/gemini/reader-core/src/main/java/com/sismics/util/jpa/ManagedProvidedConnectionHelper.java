```java
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.tool.hbm2ddl.ConnectionHelper;

public class ManagedProviderConnectionHelper implements ConnectionHelper {
    private final Properties properties;
    private ServiceRegistry serviceRegistry;
    private Connection connection;

    public ManagedProviderConnectionHelper(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void prepare(boolean needsAutoCommit) throws SQLException {
        serviceRegistry = createServiceRegistry(properties);
        connection = serviceRegistry.getService(ConnectionProvider.class).getConnection();
        if (needsAutoCommit && !connection.getAutoCommit()) {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }

    private static StandardServiceRegistryImpl createServiceRegistry(Properties properties) {
        Environment.verifyProperties(properties);
        ConfigurationHelper.resolvePlaceHolders(properties);
        return (StandardServiceRegistryImpl) new ServiceRegistryBuilder()
                .applySettings(properties)
                .buildServiceRegistry();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void release() {
        close();
    }

    private void close() {
        try {
            releaseConnection();
        } finally {
            releaseServiceRegistry();
        }
    }

    private void releaseConnection() {
        if (connection != null) {
            try {
                new SqlExceptionHelper().logAndClearWarnings(connection);
            } finally {
                try {
                    serviceRegistry.getService(ConnectionProvider.class).closeConnection(connection);
                } finally {
                    connection = null;
                }
            }
        }
    }

    private void releaseServiceRegistry() {
        if (serviceRegistry != null) {
            try {
                serviceRegistry.close();
            } finally {
                serviceRegistry = null;
            }
        }
    }
}
```