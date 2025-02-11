**Application.java**

```java
public class Application extends Application {

    private UserState userState;

    @Override
    public void onCreate() {
        super.onCreate();
        userState = new UserState();
    }

    public UserState getUserState() {
        return userState;
    }

    public static Application getInstance() {
        return (Application) getApplicationContext();
    }
}
```

**UserState.java**

```java
public class UserState {

    private boolean isLoggedIn;
    private JSONObject userInfo;

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public JSONObject getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(JSONObject userInfo) {
        this.userInfo = userInfo;
    }
}
```

**UserResource.java**

```java
// ... existing code
```

**MainActivity.java**

```java
// ... existing code

private void fetchUserInfo() {
    UserResource.info(Application.getInstance(), new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(JSONObject json) {
            Application.getInstance().getUserState().setLoggedIn(!json.optBoolean("anonymous"));
            Application.getInstance().getUserState().setUserInfo(json);
        }
    });
}
```