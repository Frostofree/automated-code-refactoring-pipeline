```java
package com.sismics.reader.agent.util;

import com.sismics.util.EnvironmentUtil;
import com.sismics.util.io.FileSettings;

public class Setting {

    private static final String READER_AGENT_PROPERTIES_FILE = "reader-agent.properties";
    private final FileSettings fileSettings;

    public Setting() throws IOException {
        String configDir = EnvironmentUtil.getConfigDir();
        fileSettings = new FileSettings(configDir, READER_AGENT_PROPERTIES_FILE);
    }

    public void read() throws IOException {
        fileSettings.read();
    }

    public void save() throws IOException {
        fileSettings.save();
    }

    public String get(String key) {
        return fileSettings.getProperty(key);
    }

    public void set(String key, String value) {
        fileSettings.setProperty(key, value);
    }

    // ... Other methods
}
```