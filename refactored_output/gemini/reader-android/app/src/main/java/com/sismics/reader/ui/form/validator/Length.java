```java
package com.sismics.reader.ui.form.validator;

import android.content.Context;

import com.sismics.reader.R;

/**
 * Text length validator.
 *
 * @author bgamard
 */
public class Length implements ValidatorType {

    private final int minLength;
    private final int maxLength;

    /**
     * Constructor.
     *
     * @param minLength
     * @param maxLength
     */
    public Length(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public boolean validate(String text) {
        return text.trim().length() >= minLength && text.trim().length() <= maxLength;
    }

    @Override
    public String getErrorMessage(Context context) {
        if (context == null) {
            return "";
        }
        int messageId = validate(null) ? R.string.validate_error_length_max : R.string.validate_error_length_min;
        return context.getResources().getString(messageId, minLength, maxLength);
    }
}
```