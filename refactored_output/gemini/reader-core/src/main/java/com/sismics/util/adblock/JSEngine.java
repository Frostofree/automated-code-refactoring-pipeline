```java
package com.sismics.util.adblock;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavaScriptEngine {

    private final ScriptEngine engine;

    public JavaScriptEngine() {
        engine = new ScriptEngineManager().getEngineByName("javascript");
    }

    public Object evaluate(String script) throws ScriptException {
        return engine.eval(script);
    }
}
```