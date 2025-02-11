```java
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

public class ReaderStandardAnalyzer {

  private final EnglishAnalyzer englishAnalyzer = new EnglishAnalyzer();
  private final StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
  private final CharArraySet stopWords;
  private final int maxTokenLength;

  public ReaderStandardAnalyzer(CharArraySet stopWords, int maxTokenLength) {
    this.stopWords = stopWords;
    this.maxTokenLength = maxTokenLength;
  }

  public TokenStream analyze(Reader reader, FieldType fieldType) {
    return fieldType.getAnalyzer().tokenStream("content", reader);
  }
}

class HtmlFieldType extends FieldType {

  private final Analyzer analyzer;

  public HtmlFieldType() {
    analyzer = new HtmlAnalyzer(stopWords, maxTokenLength);
  }

  @Override
  public Analyzer getAnalyzer() {
    return analyzer;
  }
}

class StandardFieldType extends FieldType {

  private final Analyzer analyzer;

  public StandardFieldType() {
    analyzer = new StandardAnalyzer(stopWords, maxTokenLength);
  }

  @Override
  public Analyzer getAnalyzer() {
    return analyzer;
  }
}

abstract class FieldType {

  public abstract Analyzer getAnalyzer();
}

class HtmlAnalyzer extends Analyzer {

  private final CharArraySet stopWords;
  private final int maxTokenLength;

  public HtmlAnalyzer(CharArraySet stopWords, int maxTokenLength) {
    this.stopWords = stopWords;
    this.maxTokenLength = maxTokenLength;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    StandardTokenizer tokenizer = new StandardTokenizer(Version.LUCENE_CURRENT);
    return new TokenStreamComponents(tokenizer, englishAnalyzer.tokenStream(fieldName, tokenizer));
  }
}

class StandardAnalyzer extends Analyzer {

  private final CharArraySet stopWords;
  private final int maxTokenLength;

  public StandardAnalyzer(CharArraySet stopWords, int maxTokenLength) {
    this.stopWords = stopWords;
    this.maxTokenLength = maxTokenLength;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    StandardTokenizer tokenizer = new StandardTokenizer(Version.LUCENE_CURRENT);
    tokenizer.setMaxTokenLength(maxTokenLength);
    TokenStream result = standardAnalyzer.tokenStream(fieldName, tokenizer);
    return new TokenStreamComponents(tokenizer, new StopwordAnalyzerBase(Version.LUCENE_CURRENT, stopWords).tokenStream(fieldName, result));
  }
}
```