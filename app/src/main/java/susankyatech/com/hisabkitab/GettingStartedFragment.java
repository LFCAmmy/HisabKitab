package susankyatech.com.hisabkitab;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GettingStartedFragment extends Fragment {

    @BindView(R.id.group_code)
    EditText groupCode;
    @BindView(R.id.join_group_btn)
    Button joinGroup;
    @BindView(R.id.create_group_tv)
    TextView createGroup;

    public GettingStartedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_getting_started, container, false);

        ButterKnife.bind(this, view);

        init();

        return view;
    }

    private void init() {
        joinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = groupCode.getText().toString();

                if (TextUtils.isEmpty(code)){

                }
            }
        });


        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new CreateGroupFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                //transaction.replace(R.id.content_welcome_frame, fragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });
    }

}
