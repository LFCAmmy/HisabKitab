package susankyatech.com.hisabkitab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateGroupMembersFragment extends Fragment {

    public UpdateGroupMembersFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_groups_update_group_members_recycler_view_layout, container, false);

        Button disableUserAccountBtn = view.findViewById(R.id.manage_group_update_members_disable_account_btn);
        Button deleteUserAccountBtn = view.findViewById(R.id.manage_group_update_members_delete_account_btn);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String currentUserId = mAuth.getCurrentUser().getUid();
        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);



//        disableUserAccountBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                final MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
//                        .title("Warning")
//                        .customView(R.layout.manage_group_update_group_members_disable_account_dialog_layout, true)
//                        .positiveText("Deactivate")
//                        .negativeText("Cancel")
//                        .positiveColor(getResources().getColor(R.color.green))
//                        .negativeColor(getResources().getColor(R.color.red))
//                        .show();
//
//                materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        userReference.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                                if (dataSnapshot.exists()) {
//                                    String currentGroupId = dataSnapshot.child("group_id").getValue().toString();
//
//                                    final DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId).child("members").child(currentUserId);
//                                    groupReference.child("status").setValue("inactive")
//                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//
//                                                    if (task.isSuccessful()) {
//                                                        Toast.makeText(getContext(), "Account is now deactivated!", Toast.LENGTH_SHORT).show();
//                                                    }
//                                                    else {
//                                                        Toast.makeText(getContext(), "Error occurred, please try again!", Toast.LENGTH_SHORT).show();
//                                                    }
//
//                                                }
//                                            });
//                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
//                        materialDialog.dismiss();
//                    }
//                });
//                materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        materialDialog.dismiss();
//                    }
//                });
//
//            }
//        });

        return view;
    }
}
