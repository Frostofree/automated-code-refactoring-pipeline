```java
package com.sismics.util.mime;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to check MIME types based on file magic numbers.
 *
 * @author jtremeaux
 */
public class MimeTypeUtil {
    private static final MagicNumberMatcher[] MATCHERS = {
        new MagicNumberMatcher("504B0304", MimeType.APPLICATION_ZIP),
        new MagicNumberMatcher("474946383761", MimeType.IMAGE_GIF),
        new MagicNumberMatcher("474946383961", MimeType.IMAGE_GIF),
        new MagicNumberMatcher("FFD8", MimeType.IMAGE_JPEG),
        new MagicNumberMatcher("89504E470D0A1A0A", MimeType.IMAGE_PNG),
        new MagicNumberMatcher("00000100", MimeType.IMAGE_X_ICON)
    };

    /**
     * Try to guess the MIME type of a file by its magic number (header).
     * 
     * @param file File to inspect
     * @return MIME type or null if unknown
     */
    public static String guessMimeType(File file) {
        try (var is = new FileInputStream(file)) {
            byte[] headerBytes = new byte[8];
            int readCount = is.read(headerBytes, 0, headerBytes.length);
            if (readCount <= 0) {
                return null;
            }
            String header = toHexString(headerBytes);
            for (var matcher : MATCHERS) {
                if (matcher.matches(header)) {
                    return matcher.getMimeType();
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    private static class MagicNumberMatcher {
        private final String magicNumber;
        private final MimeType mimeType;
        
        public MagicNumberMatcher(String magicNumber, MimeType mimeType) {
            this.magicNumber = magicNumber;
            this.mimeType = mimeType;
        }
        
        public boolean matches(String header) {
            return header.startsWith(magicNumber);
        }
        
        public MimeType getMimeType() {
            return mimeType;
        }
    }
}
```