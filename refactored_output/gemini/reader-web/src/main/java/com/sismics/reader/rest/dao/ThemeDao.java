```java
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Theme service.
 */
public class ThemeService {

    private static final List<String> STYLESHEETS_THEME_DIRS = Lists.newArrayList("/src/stylesheets/theme/", "/stylesheets/theme/");

    private static final FilenameFilter CSS_FILTER = (dir, name) -> name.endsWith(".css") || name.endsWith(".less");

    /**
     * Find all themes.
     *
     * @param servletContext Servlet context
     * @return List of themes
     */
    public List<String> findAll(ServletContext servletContext) {
        List<String> themes = new ArrayList<>();

        for (String themeDir : STYLESHEETS_THEME_DIRS) {
            themes.addAll(findAllThemesInDirectory(servletContext, themeDir));
        }

        return themes;
    }

    private List<String> findAllThemesInDirectory(ServletContext servletContext, String themeDir) {
        List<String> themes = new ArrayList<>();

        try {
            if (servletContext != null) {
                Set<String> fileList = servletContext.getResourcePaths(themeDir);
                if (fileList != null) {
                    for (String file : fileList) {
                        if (CSS_FILTER.accept(null, file)) {
                            themes.add(extractThemeName(file));
                        }
                    }
                }
            } else {
                URL resource = this.getClass().getResource(themeDir);
                if (resource != null) {
                    File dir = new File(resource.getFile());
                    for (File file : dir.listFiles(CSS_FILTER)) {
                        themes.add(extractThemeName(file.getName()));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unable to get themes list", e);
        }

        return themes;
    }

    private String extractThemeName(String fileName) {
        return Files.getNameWithoutExtension(fileName);
    }
}
```