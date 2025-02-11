```java
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sismics.reader.interactor.LoginManager;
import com.sismics.reader.model.application.ApplicationContext;
import com.sismics.reader.util.PreferenceUtil;
import com.sismics.reader.view.LoginViewBinder;

public class LoginActivity extends AppCompatActivity {

    private LoginViewBinder loginViewBinder;
    private LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        loginManager = new LoginManager(ApplicationContext.getInstance());
        loginViewBinder = new LoginViewBinder(findViewById(R.id.loginForm), loginManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loginManager.addObserver(loginViewBinder);
        String serverUrl = PreferenceUtil.getServerUrl(this);
        loginManager.tryConnect(serverUrl);
    }

    @Override
    protected void onStop() {
        loginManager.removeObserver(loginViewBinder);
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loginManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
```