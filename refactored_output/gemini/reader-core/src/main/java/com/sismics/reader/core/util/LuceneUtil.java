```java
package com.sismics.reader.core.util;

import com.sismics.reader.core.dao.lucene.ReaderStandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/**
 * Provides utilities for working with Apache Lucene.
 */
public class LuceneUtil {
    private static final Version LUCENE_VERSION = Version.LUCENE_42;

    /**
     * Creates an IndexWriterConfig for use with the ReaderStandardAnalyzer.
     *
     * @return the IndexWriterConfig
     */
    public static IndexWriterConfig createIndexWriterConfig() {
        return new IndexWriterConfig(LUCENE_VERSION, new ReaderStandardAnalyzer(LUCENE_VERSION));
    }

    /**
     * Sets the merge scheduler for the IndexWriterConfig to a SerialMergeScheduler.
     *
     * @param config the IndexWriterConfig to configure
     */
    public static void configureMergeScheduler(IndexWriterConfig config) {
        config.setMergeScheduler(new SerialMergeScheduler());
    }

    /**
     * Creates an IndexWriterFactory that can be used to create IndexWriters for the given Directory.
     *
     * @param directory the Directory to use
     * @return the IndexWriterFactory
     */
    public static IndexWriterFactory createIndexWriterFactory(Directory directory) {
        return new IndexWriterFactory(directory);
    }

    public static class IndexWriterFactory implements LuceneRunnable {
        private final Directory directory;

        public IndexWriterFactory(Directory directory) {
            this.directory = directory;
        }

        @Override
        public void run(IndexWriter writer) throws Exception {
            // No-op
        }

        public IndexWriter createIndexWriter(IndexWriterConfig config) throws Exception {
            return new IndexWriter(directory, config);
        }
    }

    @FunctionalInterface
    public interface LuceneRunnable {
        void run(IndexWriter writer) throws Exception;
    }
}
```