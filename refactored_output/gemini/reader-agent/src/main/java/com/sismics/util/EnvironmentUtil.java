```java
package com.sismics.util;

import java.util.Properties;

/**
 * Environment properties utilities.
 */
public class EnvironmentUtil {

    private static EnvironmentProvider environmentProvider = new SystemEnvironmentProvider();

    public static void setEnvironmentProvider(EnvironmentProvider environmentProvider) {
        EnvironmentUtil.environmentProvider = environmentProvider;
    }

    /**
     * Returns the OS version.
     *
     * @return OS version
     */
    public static String getOsVersion() {
        return environmentProvider.getOsVersion();
    }

    /**
     * Returns the home directory of the current user.
     *
     * @return Home directory
     */
    public static String getHomeDirectory() {
        return environmentProvider.getHomeDirectory();
    }

    public interface EnvironmentProvider {
        String getOsVersion();
        String getHomeDirectory();
    }

    private static class SystemEnvironmentProvider implements EnvironmentProvider {
        private final Properties properties = System.getProperties();

        @Override
        public String getOsVersion() {
            return properties.getProperty("os.version");
        }

        @Override
        public String getHomeDirectory() {
            return properties.getProperty("user.home");
        }
    }
}
```