```java
import java.util.Objects;

public class LogCriteria {

    private final String level;
    private final String tag;
    private final String message;

    public static class Builder {
        private String level;
        private String tag;
        private String message;

        public Builder level(String level) {
            this.level = Objects.requireNonNull(level, "level cannot be null").toLowerCase();
            return this;
        }

        public Builder tag(String tag) {
            this.tag = Objects.requireNonNull(tag, "tag cannot be null").toLowerCase();
            return this;
        }

        public Builder message(String message) {
            this.message = Objects.requireNonNull(message, "message cannot be null").toLowerCase();
            return this;
        }

        public LogCriteria build() {
            return new LogCriteria(level, tag, message);
        }
    }

    private LogCriteria(String level, String tag, String message) {
        this.level = level;
        this.tag = tag;
        this.message = message;
    }

    public String getLevel() {
        return level;
    }

    public String getTag() {
        return tag;
    }

    public String getMessage() {
        return message;
    }

}
```