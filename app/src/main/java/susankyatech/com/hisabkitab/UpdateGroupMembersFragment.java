package susankyatech.com.hisabkitab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class UpdateGroupMembersFragment extends Fragment {

    public UpdateGroupMembersFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_groups_update_group_members_recycler_view_layout, container, false);

        Button disableUserAccountBtn = view.findViewById(R.id.manage_group_update_members_disable_account_btn);
        Button deleteUserAccountBtn = view.findViewById(R.id.manage_group_update_members_delete_account_btn);

        final MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title("Warning")
                .customView(R.layout.manage_group_update_group_members_disable_account_dialog_layout, true)
                .positiveText("Deactivate")
                .negativeText("Cancel")
                .positiveColor(getResources().getColor(R.color.green))
                .negativeColor(getResources().getColor(R.color.red))
                .show();

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });
        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });



        return view;
    }
}
