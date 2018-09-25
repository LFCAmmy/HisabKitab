package susankyatech.com.hisabkitab.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import susankyatech.com.hisabkitab.R;
import susankyatech.com.hisabkitab.UserDataModel;

public class UpdateGroupMembers extends Fragment {

    private EditText userNameET;
    private RecyclerView recyclerView;

    private DatabaseReference groupReference;

    private String currentGroupId;

    public UpdateGroupMembers() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_group_members, container, false);

        userNameET = view.findViewById(R.id.user_name_et);
        Button proceedBtn = view.findViewById(R.id.proceed_button);
        recyclerView = view.findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentGroupId = dataSnapshot.child("group_id").getValue().toString();
                groupReference.child(currentGroupId).child("members").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Query query = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId).child("members").limitToLast(50);
                        getAllUserName(query);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String enteredName = userNameET.getText().toString();

                if (TextUtils.isEmpty(enteredName)) {
                    userNameET.setError("Please enter the username before proceeding!");
                    userNameET.requestFocus();
                } else {
                    final MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
                            .title("Update Members")
                            .customView(R.layout.proceed_dialog_layout, true)
                            .positiveText("Deactivate Account")
                            .negativeText("Delete Account")
                            .neutralText("Cancel")
                            .positiveColor(getResources().getColor(R.color.green))
                            .negativeColor(getResources().getColor(R.color.red))
                            .neutralColor(getResources().getColor(R.color.colorPrimary))
                            .canceledOnTouchOutside(false)
                            .show();

                    materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                                    .title("Warning!")
                                    .customView(R.layout.deactivate_account_dialog_layout, true)
                                    .neutralText("Cancel")
                                    .negativeText("Reactivate")
                                    .positiveText("Deactivate")
                                    .neutralColor(getResources().getColor(R.color.colorPrimary))
                                    .negativeColor(getResources().getColor(R.color.green))
                                    .positiveColor(getResources().getColor(R.color.red))
                                    .canceledOnTouchOutside(false)
                                    .show();

                            dialog.getActionButton(DialogAction.NEUTRAL).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    materialDialog.dismiss();
                                }
                            });

                            dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    groupReference.child(currentGroupId).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                String name = ds.child("name").getValue().toString();

                                                if (name.equals(enteredName)) {

                                                    String role = ds.child("role").getValue().toString();

                                                    if (role.equals("member")) {

                                                        String status = ds.child("status").getValue().toString();

                                                        if (status.equals("active")) {

                                                            groupReference.child(currentGroupId).child("members").child(ds.getKey()).child("status").setValue("inactive")
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()) {
                                                                                dialog.dismiss();
                                                                                materialDialog.dismiss();
                                                                                Toast.makeText(getActivity(), "Account Deactivated!", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        } else {
                                                            dialog.dismiss();
                                                            materialDialog.dismiss();
                                                            Toast.makeText(getActivity(), "This account is already deactivated!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    else {
                                                        dialog.dismiss();
                                                        materialDialog.dismiss();
                                                        Toast.makeText(getActivity(), "You cannot disable yourself!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });

                            dialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    groupReference.child(currentGroupId).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                String name = ds.child("name").getValue().toString();

                                                if (name.equals(enteredName)) {

                                                    String role = ds.child("role").getValue().toString();

                                                    if (role.equals("member")) {

                                                        String status = ds.child("status").getValue().toString();

                                                        if (status.equals("inactive")) {

                                                            groupReference.child(currentGroupId).child("members").child(ds.getKey()).child("status").setValue("active")
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()) {
                                                                                dialog.dismiss();
                                                                                materialDialog.dismiss();
                                                                                Toast.makeText(getActivity(), "Account Reactivated!", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        } else {
                                                            dialog.dismiss();
                                                            materialDialog.dismiss();
                                                            Toast.makeText(getActivity(), "This account is already active!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    else {
                                                        dialog.dismiss();
                                                        materialDialog.dismiss();
                                                        Toast.makeText(getActivity(), "You are always active!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });
                        }
                    });

                    materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                                    .title("Warning!")
                                    .customView(R.layout.deactivate_account_dialog_layout, true)
                                    .negativeText("Cancel")
                                    .positiveText("Delete")
                                    .negativeColor(getResources().getColor(R.color.red))
                                    .positiveColor(getResources().getColor(R.color.green))
                                    .canceledOnTouchOutside(false)
                                    .show();

                            dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    groupReference.child(currentGroupId).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            for (final DataSnapshot ds : dataSnapshot.getChildren()) {

                                                String name = ds.child("name").getValue().toString();

                                                if (name.equals(enteredName)) {

                                                    String role = ds.child("role").getValue().toString();

                                                    if (role.equals("member")) {

                                                        DatabaseReference expenditures = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures")
                                                                .child(currentGroupId).child(ds.getKey());
                                                        expenditures.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                String string = dataSnapshot.child("total_amount").getValue().toString();

                                                                if (string.equals("0")) {

                                                                    DatabaseReference deleteReference = FirebaseDatabase.getInstance().getReference().child("Group")
                                                                            .child(currentGroupId).child("members").child(ds.getKey());

                                                                    deleteReference.removeValue();

                                                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures")
                                                                            .child(currentGroupId).child(ds.getKey());

                                                                    ref.removeValue();

                                                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                                                            .child(ds.getKey());
                                                                    reference.child("group_id").setValue("none").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()) {
                                                                                dialog.dismiss();
                                                                                materialDialog.dismiss();
                                                                                Toast.makeText(getActivity(), "Member deleted from the group successfully!",
                                                                                        Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                                else {
                                                                    Toast.makeText(getActivity(), "Due amount has to be cleared of the member before deleting!",
                                                                            Toast.LENGTH_SHORT).show();
                                                                    dialog.dismiss();
                                                                    materialDialog.dismiss();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }
                                                    else {
                                                        dialog.dismiss();
                                                        materialDialog.dismiss();
                                                        Toast.makeText(getActivity(), "You are always active!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });

                            dialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    materialDialog.dismiss();
                                }
                            });
                        }
                    });

                    materialDialog.getActionButton(DialogAction.NEUTRAL).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            materialDialog.dismiss();
                        }
                    });
                }
            }
        });

        return view;
    }

    private void getAllUserName(Query query) {

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        query.addChildEventListener(childEventListener);

        FirebaseRecyclerOptions<UserDataModel> options = new FirebaseRecyclerOptions.Builder<UserDataModel>()
                .setQuery(query, UserDataModel.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<UserDataModel, UpdateGroupMembersViewHolder>(options) {

            @Override
            public UpdateGroupMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.update_group_members_recycler_view_layout, parent, false);
                return new UpdateGroupMembersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UpdateGroupMembersViewHolder holder, int position, @NonNull UserDataModel model) {
                holder.setName(model.getName());
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public static class UpdateGroupMembersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UpdateGroupMembersViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            TextView displayUserName_TV = mView.findViewById(R.id.user_name_tv);
            displayUserName_TV.setText(name);
        }
    }
}
