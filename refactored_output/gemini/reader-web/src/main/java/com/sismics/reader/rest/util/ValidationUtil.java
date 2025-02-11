```java
package com.sismics.reader.rest.util;

import com.sismics.reader.core.dao.jpa.LocaleDao;
import com.sismics.reader.rest.dao.ThemeDao;
import com.sismics.rest.exception.ClientException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;

import javax.servlet.ServletContext;
import java.text.MessageFormat;

/**
 * Utility class to validate parameters.
 *
 * @author jtremeaux
 */
public class ValidationUtil {
    private ValidationUtil() {}

    /**
     * Validates a theme.
     *
     * @param servletContext Servlet context
     * @param themeId ID of the theme to validate
     * @param nullable True if the string can be empty/null
     * @return Validated theme ID
     * @throws JSONException if JSON error
     * @throws ClientException if theme not found
     */
    public static String validateTheme(ServletContext servletContext, String themeId, boolean nullable) throws JSONException, ClientException {
        themeId = StringUtils.strip(themeId);
        if (StringUtils.isEmpty(themeId)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("Theme ID is required"));
            }
            return null;
        }

        ThemeDao themeDao = new ThemeDao();
        List<String> themeList = themeDao.findAll(servletContext);
        if (!themeList.contains(themeId)) {
            throw new ClientException("ValidationError", "Theme not found: " + themeId);
        }

        return themeId;
    }

    /**
     * Validates a locale.
     *
     * @param localeId Locale ID to validate
     * @param nullable True if string can be empty/null
     * @return Validated locale ID
     * @throws JSONException if JSON error
     * @throws ClientException if locale not found
     */
    public static String validateLocale(String localeId, boolean nullable) throws JSONException, ClientException {
        localeId = StringUtils.strip(localeId);
        if (StringUtils.isEmpty(localeId)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("Locale ID is required"));
            }
            return null;
        }

        LocaleDao localeDao = new LocaleDao();
        Locale locale = localeDao.getById(localeId);
        if (locale == null) {
            throw new ClientException("ValidationError", "Locale not found: " + localeId);
        }

        return localeId;
    }
}
```