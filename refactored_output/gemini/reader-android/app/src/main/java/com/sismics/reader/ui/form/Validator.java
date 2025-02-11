```java
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import com.sismics.reader.listener.CallbackListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to validate form elements.
 */
public class Validator {

    private final Map<View, ValidationResult> validationResults = new HashMap<>();
    private CallbackListener onValidationChanged;
    private final boolean showErrors;

    public Validator(boolean showErrors) {
        this.showErrors = showErrors;
    }

    public void setOnValidationChanged(CallbackListener onValidationChanged) {
        this.onValidationChanged = onValidationChanged;
        onValidationChanged.onComplete();
    }

    public void addValidable(final Context context, final EditText editText, ValidatorType... validatorTypes) {
        ValidationResult result = new ValidationResult(showErrors, editText);
        validationResults.put(editText, result);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate(editText, validatorTypes);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public boolean isValidated(View view) {
        ValidationResult result = validationResults.get(view);
        return result != null && result.isValidated;
    }

    public boolean isValidated() {
        for (ValidationResult result : validationResults.values()) {
            if (!result.isValidated) {
                return false;
            }
        }
        return true;
    }

    private void validate(EditText editText, ValidatorType... validatorTypes) {
        ValidationResult result = validationResults.get(editText);
        if (result == null) {
            return;
        }

        result.isValidated = true;

        for (ValidatorType validatorType : validatorTypes) {
            if (!validatorType.validate(editText.getText().toString())) {
                result.setError(validatorType.getErrorMessage(context));
                break;
            }
        }

        if (result.isValidated) {
            result.setError(null);
        }

        if (onValidationChanged != null) {
            onValidationChanged.onComplete();
        }
    }

    private static class ValidationResult {
        private boolean isValidated;
        private final boolean showErrors;
        private EditText editText;

        public ValidationResult(boolean showErrors, EditText editText) {
            this.showErrors = showErrors;
            this.editText = editText;
        }

        void setValidated(boolean validated) {
            this.isValidated = validated;
        }

        boolean isValidated() {
            return isValidated;
        }

        void setError(String error) {
            if (showErrors && !TextUtils.isEmpty(error)) {
                editText.setError(error);
            }
            isValidated = false;
        }
    }
}
```