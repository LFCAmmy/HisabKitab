package com.susankya.abhinav.myrealmapplication.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.susankya.abhinav.myrealmapplication.R;

public class ManageGroup extends Fragment {

    private Context context;

    private String currentGroupId, currentUserName;

    public ManageGroup() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_group, container, false);

        final TextView displayUserEmailTV = view.findViewById(R.id.user_email_tv);
        final TextView displayUserNameTV = view.findViewById(R.id.user_name_tv);
        final TextView displayGroupNameTV = view.findViewById(R.id.group_name_tv);
        final TextView displayCurrentMaxMembersTV = view.findViewById(R.id.group_max_members_tv);
        Button changeGroupNameBtn = view.findViewById(R.id.change_group_name_btn);
        Button changeGroupMaxMembersBtn = view.findViewById(R.id.change_max_members_btn);
        Button updateGroupMembersBtn = view.findViewById(R.id.update_group_members_btn);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        final DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference().child("Group");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue(String.class);
                    currentUserName = dataSnapshot.child("user_name").getValue(String.class);
                    String userEmail = dataSnapshot.child("user_email").getValue(String.class);

                    displayUserEmailTV.setText(userEmail);
                    displayUserNameTV.setText(currentUserName);

                    groupReference.child(currentGroupId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                String currentGroupName = dataSnapshot.child("group_name").getValue(String.class);
                                long currentMaxMembers = Integer.valueOf(dataSnapshot.child("max_members").getValue().toString());

                                displayGroupNameTV.setText(currentGroupName);
                                displayCurrentMaxMembersTV.setText(String.valueOf(currentMaxMembers));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        changeGroupNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MaterialDialog dialog = new MaterialDialog.Builder(context)
                        .title("Change Group Name")
                        .customView(R.layout.change_group_name_dialog_layout, true)
                        .negativeText("Change")
                        .positiveText("Cancel")
                        .negativeColor(getResources().getColor(R.color.green))
                        .positiveColor(getResources().getColor(R.color.red))
                        .canceledOnTouchOutside(false)
                        .show();

                final EditText changeGroupNameET = dialog.getCustomView().findViewById(R.id.group_name_et);

                dialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String currentGroupName = changeGroupNameET.getText().toString();

                        if (TextUtils.isEmpty(currentGroupName)) {
                            changeGroupNameET.setError("Please enter a new group name!");
                            changeGroupNameET.requestFocus();
                        } else if (currentGroupName.length() > 10) {
                            changeGroupNameET.setError("New group name must be of maximum 10 characters only!");
                            changeGroupNameET.requestFocus();
                        } else {
                            groupReference.child(currentGroupId).child("group_name").setValue(currentGroupName);
                            dialog.dismiss();
                            Snackbar.make(view, "Group name changed successfully!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        changeGroupMaxMembersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MaterialDialog dialog = new MaterialDialog.Builder(context)
                        .title("Change Max Members")
                        .customView(R.layout.change_max_members_dialog_layout, true)
                        .negativeText("Change")
                        .positiveText("Cancel")
                        .negativeColor(getResources().getColor(R.color.green))
                        .positiveColor(getResources().getColor(R.color.red))
                        .canceledOnTouchOutside(false)
                        .show();

                final EditText changeMaxGroupMembersET = dialog.getCustomView().findViewById(R.id.max_members_et);

                dialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String currentGroupMaxMembers = changeMaxGroupMembersET.getText().toString();

                        if (TextUtils.isEmpty(currentGroupMaxMembers)) {
                            changeMaxGroupMembersET.setError("Please enter new max members!");
                            changeMaxGroupMembersET.requestFocus();
                        } else if (Integer.valueOf(currentGroupMaxMembers) > 10) {
                            changeMaxGroupMembersET.setError("Max members must be less than or equal to 10!");
                            changeMaxGroupMembersET.requestFocus();
                        } else {
                            groupReference.child(currentGroupId).child("max_members").setValue(currentGroupMaxMembers);
                            dialog.dismiss();
                            Snackbar.make(view, "Max members changed successfully!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        updateGroupMembersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_main_frame, new UpdateGroupMembers());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }
}
