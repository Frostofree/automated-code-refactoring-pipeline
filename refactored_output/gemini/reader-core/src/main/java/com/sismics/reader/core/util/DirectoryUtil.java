```java
public interface FileManager {
    File getBaseDataDirectory(EnvironmentManager environmentManager);
}

public interface EnvironmentManager {
    File getReaderHome();

    boolean isUnitTest();

    boolean isUnix();

    boolean isWindows();

    boolean isMacOs();

    String getWindowsAppData();

    String getMacOsUserHome();
}

class DefaultFileManager implements FileManager {
    private static final String DEFAULT_SUBDIRECTORY_NAME = "Reader";

    @Override
    public File getBaseDataDirectory(EnvironmentManager environmentManager) {
        File baseDataDir = null;
        if (StringUtils.isNotBlank(environmentManager.getReaderHome())) {
            baseDataDir = new File(environmentManager.getReaderHome());
        } else if (environmentManager.isUnitTest()) {
            baseDataDir = new File(System.getProperty("java.io.tmpdir"));
        } else {
            // We are in a webapp environment and nothing is specified, use the default directory for this OS
            baseDataDir = EnvironmentUtil.getDefaultDataDirectory(environmentManager);
        }

        baseDataDir = createDirectoryIfNotExists(baseDataDir, DEFAULT_SUBDIRECTORY_NAME);
        return baseDataDir;
    }

    protected File createDirectoryIfNotExists(File directory, String subdirectoryName) {
        File subdirectory = new File(directory.getPath() + File.separator + subdirectoryName);
        if (!subdirectory.isDirectory()) {
            subdirectory.mkdirs();
        }
        return subdirectory;
    }
}

class SubdirectoryFileManager implements FileManager {
    private FileManager fileManager;
    private String subdirectoryName;

    public SubdirectoryFileManager(FileManager fileManager, String subdirectoryName) {
        this.fileManager = fileManager;
        this.subdirectoryName = subdirectoryName;
    }

    @Override
    public File getBaseDataDirectory(EnvironmentManager environmentManager) {
        return fileManager.createDirectoryIfNotExists(fileManager.getBaseDataDirectory(environmentManager), subdirectoryName);
    }
}

class DbDirectoryManager extends SubdirectoryFileManager {
    public DbDirectoryManager(FileManager fileManager) {
        super(fileManager, "db");
    }
}

class FaviconDirectoryManager extends SubdirectoryFileManager {
    public FaviconDirectoryManager(FileManager fileManager) {
        super(fileManager, "favicon");
    }
}

class LuceneDirectoryManager extends SubdirectoryFileManager {
    public LuceneDirectoryManager(FileManager fileManager) {
        super(fileManager, "lucene");
    }
}

class LogDirectoryManager extends SubdirectoryFileManager {
    public LogDirectoryManager(FileManager fileManager) {
        super(fileManager, "log");
    }
}
```