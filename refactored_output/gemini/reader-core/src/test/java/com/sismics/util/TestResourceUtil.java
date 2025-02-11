```java
package com.sismics.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertTrue;

/**
 * Test of the resource utils.
 *
 * @author jtremeaux
 */
public class TestResourceUtil {

    @Test
    public void listFilesTest() throws Exception {
        List<String> fileList = ResourceUtil.list(Test.class, "/junit/framework");
        assertTrue(fileList.contains("Test.class"));

        fileList = ResourceUtil.list(Test.class, "/junit/framework/");
        assertTrue(fileList.contains("Test.class"));

        fileList = ResourceUtil.list(Test.class, "junit/framework/");
        assertTrue(fileList.contains("Test.class"));

        fileList = ResourceUtil.list(Test.class, "junit/framework/");
        assertTrue(fileList.contains("Test.class"));
    }

    @Test
    public void loadResourceFileTest() {
        Map<Object, Object> properties = ResourceUtil.loadPropertiesFromUrl(TestResourceUtil.class.getResource("/config.properties"));
        assertTrue(properties.size() > 0);
    }

    private static class ResourceUtil {
        private static final String CLASS_PATH_PREFIX = "classpath:";

        private ResourceUtil() {
            // Not instantiable
        }

        /**
         * List files in a package.
         */
        public static List<String> list(Class<?> klass, String packageName) {
            String path = packageName.replace('.', '/');
            ClassLoader classLoader = klass.getClassLoader();

            List<String> result = Lists.newArrayList();
            for (URL resource : getResources(classLoader, CLASS_PATH_PREFIX + path)) {
                if (resource.toString().endsWith("/")) {
                    for (String fileName : listFilesInDirectory(resource)) {
                        result.add(path + '/' + fileName);
                    }
                } else {
                    result.add(path + '/' + resource.getPath().substring(resource.getPath().lastIndexOf('/') + 1));
                }
            }
            return result;
        }

        /**
         * Get the URL of a classpath resource.
         */
        public static URL[] getResources(ClassLoader classLoader, String resourcePath) {
            try {
                return classLoader.getResources(resourcePath.substring(CLASS_PATH_PREFIX.length()));
            } catch (IOException e) {
                return new URL[0];
            }
        }

        /**
         * List files from a directory resource.
         */
        public static List<String> listFilesInDirectory(URL directory) {
            List<String> result = Lists.newArrayList();
            try (InputStream in = directory.openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.add(line);
                }
            } catch (IOException e) {
                // Ignore
            }
            return result;
        }

        /**
         * Load properties from a URL.
         */
        public static Map<Object, Object> loadPropertiesFromUrl(URL url) {
            Map<Object, Object> properties = Maps.newHashMap();
            try (InputStream in = url.openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                Pattern pattern = Pattern.compile("([^\\s=]+)[\\s=]+(.*)");
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        java.util.regex.Matcher matcher = pattern.matcher(line);
                        if (matcher.matches()) {
                            properties.put(matcher.group(1), matcher.group(2));
                        }
                    }
                }
            } catch (IOException e) {
                // Ignore
            }
            return properties;
        }
    }
}
```