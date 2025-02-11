```java
package com.sismics.reader.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.reader.R;
import com.sismics.reader.api.CategoryApi;
import com.sismics.reader.api.CategoryResource;
import com.sismics.reader.api.SubscriptionResource;
import com.sismics.reader.ui.adapter.CategoryAdapter;
import com.sismics.reader.ui.fragment.CategoriesFragment;

import org.json.JSONObject;

import java.util.UUID;

public class CategoriesActivity extends Activity implements CategoryAdapter.Callback {

    private CategoryApi categoryApi;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_activity);

        adapter = CategoriesFragment.createAdapter(this);
        categoryApi = new CategoryApi(this, adapter);
        categoryApi.list();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.accept:
                save();
                return true;
            case R.id.add_category:
                final EditText input = new EditText(this);
                input.setHint(R.string.category_name);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.add_category)
                        .setView(input)
                        .setPositiveButton(R.string.ok, (dialog, whichButton) -> addCategory(input.getText().toString()))
                        .setNegativeButton(R.string.cancel, (dialog, whichButton) -> {})
                        .show();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SubscriptionResource.cancel(this);
    }

    private void save() {
        if (adapter.getStates().size() > 0) {
            ProgressDialog progressDialog = ProgressDialog.show(this,
                    null, null, true, true, dialog -> {
                        categoryApi.cancel();
                        dialog.dismiss();
                        adapter.clearStates();
                        setResult(RESULT_OK);
                        finish();
                    });

            final Runnable runnable = () -> {
                if (!progressDialog.isShowing()) {
                    return;
                }

                final CategoryAdapter.State state = adapter.getStates().peek();
                if (state == null) {
                    Toast.makeText(CategoriesActivity.this, R.string.manage_categories_save_success, Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    progressDialog.dismiss();
                    finish();
                    return;
                }

                progressDialog.setMessage(getString(R.string.manage_categories_saving, adapter.getStates().size()));

                final JsonHttpResponseHandler callback = new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        adapter.getStates().poll();
                        runnable.run();
                    }

                    @Override
                    public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(CategoriesActivity.this, R.string.manage_categories_save_error, Toast.LENGTH_LONG).show();
                        adapter.clearStates();
                        progressDialog.dismiss();
                        finish();
                    }
                };

                switch (state.getType()) {
                    case ADD:
                        categoryApi.add(state.getName(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                adapter.updateCategoryId(state.getId(), response.optString("id"));
                                callback.onSuccess(response);
                            }

                            @Override
                            public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] responseBody, Throwable error) {
                                callback.onFailure(statusCode, headers, responseBody, error);
                            }
                        });
                        break;

                    case DELETE:
                        categoryApi.delete(state.getId(), callback);
                        break;

                    case MOVE:
                        categoryApi.update(state.getId(), null, state.getPosition(), callback);
                        break;

                    case RENAME:
                        categoryApi.update(state.getId(), state.getName(), null, callback);
                        break;
                }
            };
            runnable.run();
            return;
        }

        setResult(RESULT_CANCELED);
        finish();
    }

    private void addCategory(String name) {
        adapter.add(name);
    }

    @Override
    public void onCategoryAdded(UUID id) {
        categoryApi.add(id, null);
    }

    @Override
    public void onCategoryDeleted(UUID id) {
        categoryApi.delete(id);
    }

    @Override
    public void onCategoryMoved(UUID id, int position) {
        categoryApi.update(id, null, position);
    }

    @Override
    public void onCategoryRenamed(UUID id, String name) {
        categoryApi.update(id, name, null);
    }
}
```