package susankyatech.com.hisabkitab.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import susankyatech.com.hisabkitab.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateGroupFragment extends Fragment {


    public CreateGroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        return view;
    }

}
