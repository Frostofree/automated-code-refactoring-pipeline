```java
package com.sismics.reader.core.dao.lucene;

import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.util.IndexWriterUtil;
import com.sismics.reader.core.util.jpa.PaginatedList;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.StandardQueryParser;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleDao {

    private static final FieldType FIELD_TYPE = new FieldType(TextField.TYPE_STORED);

    static {
        FIELD_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    }

    public void rebuildIndex(List<Article> articleList) {
        doIndexOperation(writer -> {
            try {
                // Empty index
                writer.deleteAll();

                // Add all articles
                for (Article article : articleList) {
                    Document document = getDocumentFromArticle(article);
                    writer.addDocument(document);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void create(List<Article> articleList) {
        doIndexOperation(writer -> {
            try {
                for (Article article : articleList) {
                    Document document = getDocumentFromArticle(article);
                    writer.addDocument(document);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void update(List<Article> articleList) {
        doIndexOperation(writer -> {
            try {
                for (Article article : articleList) {
                    Document document = getDocumentFromArticle(article);
                    writer.updateDocument(new Term("id", article.getId()), document);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void delete(List<Article> articleList) {
        doIndexOperation(writer -> {
            try {
                for (Article article : articleList) {
                    writer.deleteDocuments(new Term("id", article.getId()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void doIndexOperation(IndexWriterUtil.Operation operation) {
        IndexWriterUtil.doWithWriter(operation);
    }

    public Map<String, Article> search(PaginatedList<UserArticleDto> paginatedList, String searchQuery) {
        // Escape query and add quotes so QueryParser generate a PhraseQuery
        searchQuery = "\"" + QueryParserUtil.escape(searchQuery) + "\"";

        // Build search query
        StandardQueryParser qpHelper = new StandardQueryParser(new ReaderStandardAnalyzer(Version.LUCENE_42));
        qpHelper.setPhraseSlop(100000); // PhraseQuery add terms
        Query titleQuery = qpHelper.parse(searchQuery, "title");
        Query descriptionQuery = qpHelper.parse(searchQuery, "description");

        // Search on article content
        BooleanQuery query = new BooleanQuery();
        query.add(titleQuery, Occur.SHOULD);
        query.add(descriptionQuery, Occur.SHOULD);

        return searchArticles(paginatedList, query);
    }

    private Map<String, Article> searchArticles(PaginatedList<UserArticleDto> paginatedList, Query query) {
        // Searching
        Map<String, Article> articleList = new HashMap<>();
        try (IndexSearcher searcher = new IndexSearcher(IndexWriterUtil.getDirectoryReader());
             DirectoryReader reader = searcher.getIndexReader()) {

            TopDocs topDocs = searcher.search(query, paginatedList.getOffset() + paginatedList.getLimit());
            ScoreDoc[] scoreDocs = new ScoreDoc[topDocs.scoreDocs.length - paginatedList.getOffset()];
            System.arraycopy(topDocs.scoreDocs, paginatedList.getOffset(), scoreDocs, 0, scoreDocs.length);
            topDocs = new TopDocs(topDocs.totalHits, scoreDocs, topDocs.getMaxScore());
            int total = topDocs.totalHits;
            paginatedList.setResultCount(total);

            // Extract article ids
            addArticlesToList(reader, scoreDocs, articleList);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while searching articles", e);
        }
        return articleList;
    }

    private void addArticlesToList(DirectoryReader reader, ScoreDoc[] scoreDocs, Map<String, Article> articleList) throws IOException {
        for (ScoreDoc scoreDoc : scoreDocs) {
            String id = reader.document(scoreDoc.doc).get("id");
            Article article = new Article();
            article.setId(id);
            articleList.put(id, article);
        }
    }

    private org.apache.lucene.document.Document getDocumentFromArticle(Article article) {
        // Building document
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
        document.add(new StringField("id", article.getId(), Field.Store.YES));
        document.add(new StringField("url", article.getUrl(), Field.Store.YES));
        document.add(new LongField("date", article.getPublicationDate().getTime(), Field.Store.YES));
        document.add(new Field("title", article.getTitle(), FIELD_TYPE));
        document.add(new Field("description", article.getDescription(), FIELD_TYPE));
        return document;
    }
}
```