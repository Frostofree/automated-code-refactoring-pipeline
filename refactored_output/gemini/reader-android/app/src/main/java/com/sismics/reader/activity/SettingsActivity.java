```java
package com.sismics.reader.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sismics.reader.fragment.SettingsFragment;

/**
 * Settings activity.
 *
 * @author bgamard.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
```