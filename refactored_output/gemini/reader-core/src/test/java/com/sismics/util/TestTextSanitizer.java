```java
import com.sismics.reader.core.util.sanitizer.TextSanitizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class TestTextSanitizer {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { null, "" },
                { "Test title", "Test title" },
                { "Test <pre>title</pre>", "Test title" },
                { "Test title &mdash; a title", "Test title — a title" },
                { "Weirdest DLC Sponsorship Ever: SimCity&lt;/em&gt;, Brought To You By Crest",
                        "Weirdest DLC Sponsorship Ever: SimCity, Brought To You By Crest" },
        });
    }

    private final String input;
    private final String expected;

    public TestTextSanitizer(String input, String expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void textSanitizerTest() {
        assertEquals(expected, TextSanitizer.sanitize(input));
    }
}
```