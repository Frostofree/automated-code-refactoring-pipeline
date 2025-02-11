package com.sismics.reader.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sismics.reader.api.LoginApi;
import com.sismics.reader.api.UserApi;

/**
 * Access to /user API.
 *
 * @author bgamard
 */
public class UserResource {

    private static LoginApi loginApi;
    private static UserApi userApi;

    private static void init(Context context) {
        if (loginApi == null) {
            loginApi = new LoginApi(context);
        }
        if (userApi == null) {
            userApi = new UserApi(context);
        }
    }

    /**
     * POST /user/login.
     *
     * @param context Context
     * @param username Username
     * @param password Password
     * @param responseHandler Callback
     */
    public static void login(Context context, String username, String password, JsonHttpResponseHandler responseHandler) {
        init(context);
        loginApi.login(username, password, responseHandler);
    }

    /**
     * GET /user.
     *
     * @param context Context
     * @param responseHandler Callback
     */
    public static void info(Context context, JsonHttpResponseHandler responseHandler) {
        init(context);
        userApi.info(responseHandler);
    }

    /**
     * POST /user/logout.
     *
     * @param context Context
     * @param responseHandler Callback
     */
    public static void logout(Context context, JsonHttpResponseHandler responseHandler) {
        init(context);
        userApi.logout(responseHandler);
    }
}