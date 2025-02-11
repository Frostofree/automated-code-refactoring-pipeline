```java
package com.sismics.reader.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Utility class to retrieve application version information.
 */
public class ApplicationVersionUtil {

    private ApplicationVersionUtil() {
    }

    /**
     * Returns the version name of the application.
     *
     * @param context the application context
     * @return the version name
     */
    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    /**
     * Returns the version code of the application.
     *
     * @param context the application context
     * @return the version code
     */
    public static int getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }
}
```