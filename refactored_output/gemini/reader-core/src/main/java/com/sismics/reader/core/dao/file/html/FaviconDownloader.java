```java
interface FaviconExtractor {

    URL getFaviconUrl();
}

interface FaviconDownloader {

    String downloadFavicon(String faviconUrl);
}

class UrlFaviconExtractor implements FaviconExtractor {

    private final URL pageUrl;

    public UrlFaviconExtractor(URL pageUrl) {
        this.pageUrl = pageUrl;
    }

    @Override
    public URL getFaviconUrl() {
        // TODO: Implement
        return null;
    }
}

class ReaderHttpClientFaviconExtractor implements FaviconExtractor {

    private final ReaderHttpClient client;
    private final URL pageUrl;

    public ReaderHttpClientFaviconExtractor(ReaderHttpClient client, URL pageUrl) {
        this.client = client;
        this.pageUrl = pageUrl;
    }

    @Override
    public URL getFaviconUrl() {
        // TODO: Implement
        return null;
    }
}

class ReaderHttpClientFaviconDownloader implements FaviconDownloader {

    private final String directory;
    private final String fileName;
    private final int timeout;

    public ReaderHttpClientFaviconDownloader(String directory, String fileName, int timeout) {
        this.directory = directory;
        this.fileName = fileName;
        this.timeout = timeout;
    }

    @Override
    public String downloadFavicon(String faviconUrl) {
        try {
            client.setTimeout(timeout);
            return client.open(new URL(faviconUrl), this::process);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Error downloading favicon at URL {0}", faviconUrl), e);
            }
        }
        return null;
    }

    private String process(InputStream is) throws Exception {
        File localFile = null;
        try {
            localFile = File.createTempFile("reader_favicon", ".ico");
            if (ByteStreams.copy(is, new FileOutputStream(localFile)) > 0) {
                String type = MimeTypeUtil.guessMimeType(localFile);
                if (type != null) {
                    String extension = FAVICON_MIME_TYPE_MAP.get(type);
                    if (extension != null) {
                        File outputFile = new File(directory + File.separator + fileName + extension);
                        Files.copy(localFile, new FileOutputStream(outputFile));
                        return outputFile.getPath();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Favicon file not found at URL {0}", faviconUrl));
            }
        } finally {
            if (localFile != null) {
                try {
                    localFile.delete();
                } catch (Exception e) {
                    // NOP
                }
            }
        }
        return null;
    }
}

class ReaderHttpClient {

    protected int timeout = 3000;

    public ReaderHttpClient() {
    }

    public ReaderHttpClient(int timeout) {
        this.timeout = timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public <T> T open(URL url, Function<InputStream, T> processor) throws Exception {
        // TODO: Implement
        return null;
    }
}
```