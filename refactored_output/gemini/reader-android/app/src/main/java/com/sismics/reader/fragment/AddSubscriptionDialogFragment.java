```java
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sismics.reader.R;
import com.sismics.reader.util.SubscriptionDialogUtil;

import org.json.JSONObject;

/**
 * Add subscription dialog fragment.
 *
 * @author bgamard.
 */
public class AddSubscriptionDialogFragment extends DialogFragment {

    private AddSubscriptionDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the dialog view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_subscription_dialog_fragment, null);

        // Create the dialog and provide the button click listeners to control the dismiss behavior
        builder.setTitle(R.string.add_subscription)
                .setView(view)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText urlEditText = view.findViewById(R.id.subscriptionUrlEditText);
                        String url = urlEditText.getText().toString();
                        SubscriptionDialogUtil.addSubscription(getActivity(), url, listener);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, cancel button closes dialog
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        // Cancel pending requests to subscription API
        SubscriptionDialogUtil.cancel(getActivity());
    }

    public void setListener(AddSubscriptionDialogListener listener) {
        this.listener = listener;
    }

    public interface AddSubscriptionDialogListener {
        public void onSubscriptionAdded(JSONObject json);
    }
}
```