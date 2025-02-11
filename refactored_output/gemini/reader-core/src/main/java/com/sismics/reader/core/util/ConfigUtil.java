```java
package com.sismics.reader.core.util;

import java.util.ResourceBundle;

import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.model.jpa.Config;

/**
 * Configuration parameter utilities.
 * 
 * @author jtremeaux
 */
public class ConfigUtil {

    private static final ConfigDao CONFIG_DAO = new ConfigDao();
    private static final ResourceBundle CONFIG_BUNDLE = ResourceBundle.getBundle("config");
    
    /**
     * Returns the configuration parameter with the specified type.
     * 
     * @param configType Type of the configuration parameter
     * @return Configuration parameter
     * @throws IllegalStateException Configuration parameter undefined
     */
    private static Config getConfig(ConfigType configType) {
        Config config = CONFIG_DAO.getById(configType);
        if (config == null) {
            throw new IllegalStateException("Config parameter not found: " + configType);
        }
        return config;
    }
    
    /**
     * Returns the value of a configuration parameter as a string.
     * 
     * @param configType Type of the configuration parameter
     * @return Value of the configuration parameter
     */
    public static String getConfigValueAsString(ConfigType configType) {
        return getConfig(configType).getValue();
    }

    /**
     * Returns the value of a configuration parameter as an integer.
     * 
     * @param configType Type of the configuration parameter
     * @return Value of the configuration parameter
     */
    public static int getConfigValueAsInt(ConfigType configType) {
        return Integer.parseInt(getConfigValueAsString(configType));
    }

    /**
     * Returns the value of a configuration parameter as a boolean.
     * 
     * @param configType Type of the configuration parameter
     * @return Value of the configuration parameter
     */
    public static boolean getConfigValueAsBoolean(ConfigType configType) {
        return Boolean.parseBoolean(getConfigValueAsString(configType));
    }
}
```