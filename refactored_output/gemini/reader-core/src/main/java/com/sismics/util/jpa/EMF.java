```java
import org.hibernate.cfg.Environment;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Entity manager factory.
 *
 * @author jtremeaux
 */
public final class EMF {
    private static final Logger log = LoggerFactory.getLogger(EMF.class);

    private static Map<Object, Object> properties;
    private static EntityManagerFactory emfInstance;

    static {
        try {
            initProperties();

            Environment.verifyProperties(properties);
            ConfigurationHelper.resolvePlaceHolders(properties);
            ServiceRegistry reg = new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();

            emfInstance = Persistence.createEntityManagerFactory("transactions-optional", properties);

        } catch (Throwable t) {
            log.error("Error creating EMF", t);
        }
    }

    private static void initProperties() throws IOException {
        // Use external properties file if it exists
        String propertiesFile = EnvironmentUtil.getHibernateProperties();
        if (propertiesFile != null) {
            log.info("Loading hibernate.properties from location: " + propertiesFile);
            try {
                URL hibernatePropertiesUrl = new URL(propertiesFile);
                properties = PropertyLoader.loadPropertiesFromUrl(hibernatePropertiesUrl);
                return;
            } catch (Exception e) {
                log.error("Error loading external hibernate.properties: " + propertiesFile, e);
            }
        }

        // Use properties file packaged with the app if it exists
        URL hibernatePropertiesUrl = EMF.class.getResource("/hibernate.properties");
        if (hibernatePropertiesUrl != null) {
            log.info("Configuring EntityManager from packaged hibernate.properties: " + hibernatePropertiesUrl);
            properties = PropertyLoader.loadPropertiesFromUrl(hibernatePropertiesUrl);
            return;
        }

        // Otherwise, use environment parameters
        log.info("Configuring EntityManager from environment parameters");
        properties = getEntityManagerPropertiesFromEnvironment();
    }

    private static Map<Object, Object> getEntityManagerPropertiesFromEnvironment() {
        Map<Object, Object> props = PropertyLoader.loadPropertiesFromEnvironment();
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.max_fetch_depth", "5");
        props.put("hibernate.cache.use_second_level_cache", "false");
        return props;
    }

    /**
     * Private constructor.
     */
    private EMF() {
    }

    /**
     * Returns an instance of EMF.
     *
     * @return Instance of EMF
     */
    public static EntityManagerFactory get() {
        return emfInstance;
    }
}
```