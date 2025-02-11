**Validator.java**

```java
import com.google.common.base.Strings;
import com.sismics.rest.exception.ClientException;
import java.text.MessageFormat;

public class Validator {

    // ...

    public static void validateLength(String s, String name, Integer min, Integer max, boolean nullable) throws ClientException {
        validateNotBlank(s, name);
        if (min != null && s.length() < min) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be at least {1} characters long", name, min));
        }
        if (max != null && s.length() > max) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be at most {1} characters long", name, max));
        }
    }

    public static void validateNotBlank(String s, String name) throws ClientException {
        if (Strings.isNullOrEmpty(s)) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} cannot be blank", name));
        }
    }
}
```