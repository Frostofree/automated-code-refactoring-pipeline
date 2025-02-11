```java
package com.sismics.reader.ui.form.validator;

import java.util.regex.Pattern;

import android.content.Context;

/**
 * Email validator.
 *
 * @author bgamard
 */
public class EmailValidator implements ValidatorType {

    /**
     * Pattern de validation.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");

    @Override
    public boolean validate(String text) {
        return EMAIL_PATTERN.matcher(text).matches();
    }
}
```