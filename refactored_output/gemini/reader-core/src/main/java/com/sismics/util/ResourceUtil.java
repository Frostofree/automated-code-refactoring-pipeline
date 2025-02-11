```java
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceUtil {

    private ResourceUtil() {
    }

    /**
     * List files inside a directory. The path can be a directory on the filesystem, or inside a JAR.
     *
     * @param clazz Class
     * @param path  Path
     * @return List of files
     */
    public static List<String> listFilesInDirectory(Class<?> clazz, String path)
            throws MalformedURLException, IOException {
        URL dirUrl = clazz.getResource(path);
        if (dirUrl == null) {
            throw new IOException("Cannot find resource: " + path);
        }

        return Stream.of(new File(dirUrl.toURI()).listFiles())
                .filter((file) -> file.isFile())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    /**
     * List files inside a JAR.
     *
     * @param clazz Class
     * @param path  Path
     * @return List of files
     */
    public static List<String> listFilesInJar(Class<?> clazz, String path)
            throws IOException, URISyntaxException {
        URL dirUrl = clazz.getResource(path);
        if (dirUrl == null) {
            throw new IOException("Cannot find resource: " + path);
        }

        String jarPath = dirUrl.getPath().substring(5, dirUrl.getPath().indexOf("!"));
        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
        Set<String> fileSet = new HashSet<>();

        try {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName();
                if (!entryName.startsWith(path)) {
                    continue;
                }
                String name = entryName.substring(path.length());
                if (!"".equals(name)) {
                    int checkSubdir = name.indexOf("/");
                    if (checkSubdir >= 0) {
                        name = name.substring(0, checkSubdir);
                    }
                    fileSet.add(name);
                }
            }
        } finally {
            jar.close();
        }
        return Lists.newArrayList(fileSet);
    }

    /**
     * Retrieve files inside a directory or JAR.
     *
     * @param clazz Class
     * @param path  Path
     * @return List of files
     */
    public static List<String> listFiles(Class<?> clazz, String path)
            throws IOException, URISyntaxException {
        try {
            return listFilesInDirectory(clazz, path);
        } catch (IOException e) {
            return listFilesInJar(clazz, path);
        }
    }

    /**
     * Retrieve files inside a directory or JAR. The path can be a directory on the filesystem, or inside a JAR.
     *
     * @param clazz   Class
     * @param path    Path
     * @param filter  Filter
     * @return List of files
     */
    public static List<String> listFiles(Class<?> clazz, String path, FilenameFilter filter)
            throws MalformedURLException, IOException {
        URL dirUrl = clazz.getResource(path);
        if (dirUrl == null) {
            throw new IOException("Cannot find resource: " + path);
        }

        if (dirUrl.getProtocol().equals("file")) {
            return Arrays.asList(new File(dirUrl.toURI()).list(filter));
        }

        if (dirUrl.getProtocol().equals("jar")) {
            return listFilesFromJar(dirUrl, path, filter);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirUrl);
    }

    private static List<String> listFilesFromJar(URL dirUrl, String path, FilenameFilter filter)
            throws IOException {
        String jarPath = dirUrl.getPath().substring(5, dirUrl.getPath().indexOf("!"));
        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));

        try {
            Enumeration<JarEntry> entries = jar.entries();

            List<String> fileNames = new ArrayList<>();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName();
                if (!entryName.startsWith(path)) {
                    continue;
                }
                String name = entryName.substring(path.length());
                if (!"".equals(name)) {
                    int checkSubdir = name.indexOf("/");
                    if (checkSubdir >= 0) {
                        name = name.substring(0, checkSubdir);
                    }

                    if (filter == null || filter.accept(null, name)) {
                        fileNames.add(name);
                    }
                }
            }

            return fileNames;
        } finally {
            jar.close();
        }
    }
}

class PropertiesUtil {
    private PropertiesUtil() {
    }

    /**
     * Load a properties file from an URL
     *
     * @param url The URL
     * @return The properties file
     */
    public static Properties loadPropertiesFromUrl(URL url) throws RuntimeException {
        InputStream is = null;
        try {
            is = url.openStream();
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Cannot load properties file for url: " + url, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // NOP
                }
            }
        }
    }
}
```