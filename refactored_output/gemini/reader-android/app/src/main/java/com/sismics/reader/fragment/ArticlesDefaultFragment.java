```
package com.sismics.reader.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sismics.reader.R;

/**
 * Articles default fragment.
 *
 * @author bgamard
 */
public class ArticlesDefaultFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.articles_fragment, container, false);
    }
}
```