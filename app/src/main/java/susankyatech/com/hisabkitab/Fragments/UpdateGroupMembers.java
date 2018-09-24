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
    private Button proceedBtn;

    private RecyclerView recyclerView;

    private DatabaseReference groupReference;

    private String currentGroupId, userStatus = "enable";

    public UpdateGroupMembers() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_group_members, container, false);

        userNameET = view.findViewById(R.id.user_name_et);
        proceedBtn = view.findViewById(R.id.proceed_button);

        recyclerView = view.findViewById(R.id.recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                                            .customView(R.layout.deactivate_member_dialog_layout, true)
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

                                            groupReference.child(currentGroupId).child("members").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    for (DataSnapshot ds: dataSnapshot.getChildren()) {

                                                        String name = ds.child("name").getValue().toString();

                                                        if (name.equals(enteredName)) {

                                                            String role = ds.child("role").getValue().toString();

                                                            if (!role.equals("admin")) {

                                                                String status = ds.child("status").getValue().toString();

                                                                if (status.equals("inactive")) {
                                                                    Toast.makeText(getActivity(), "This account is already deactivated!", Toast.LENGTH_SHORT).show();
                                                                    dialog.dismiss();
                                                                    materialDialog.dismiss();
                                                                }
                                                                else {
                                                                    groupReference.child(currentGroupId).child("members").child(ds.getKey()).child("status")
                                                                            .setValue("inactive").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()) {
                                                                                dialog.dismiss();
                                                                                materialDialog.dismiss();
                                                                                Toast.makeText(getActivity(), "Account deactivated successfully!", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            } else {
                                                                dialog.dismiss();
                                                                materialDialog.dismiss();
                                                                Toast.makeText(getActivity(), "You cannot deactivated yourself!", Toast.LENGTH_SHORT).show();
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

                                            groupReference.child(currentGroupId).child("members").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    for (DataSnapshot ds: dataSnapshot.getChildren()) {

                                                        String name = ds.child("name").getValue().toString();

                                                        if (name.equals(enteredName)) {

                                                            String role = ds.child("role").getValue().toString();

                                                            if (!role.equals("admin")) {

                                                                String status = ds.child("status").getValue().toString();

                                                                if (status.equals("active")) {
                                                                    Toast.makeText(getActivity(), "This account is already active!", Toast.LENGTH_SHORT).show();
                                                                    dialog.dismiss();
                                                                    materialDialog.dismiss();
                                                                }
                                                                else {
                                                                    groupReference.child(currentGroupId).child("members").child(ds.getKey()).child("status")
                                                                            .setValue("inactive").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()) {
                                                                                dialog.dismiss();
                                                                                materialDialog.dismiss();
                                                                                Toast.makeText(getActivity(), "Account reactivated successfully!", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            } else {
                                                                dialog.dismiss();
                                                                materialDialog.dismiss();
                                                                Toast.makeText(getActivity(), "You cannot deactivated yourself!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                        else {
                                                            dialog.dismiss();
                                                            materialDialog.dismiss();
                                                            Toast.makeText(getActivity(), "Please enter a valid group member!", Toast.LENGTH_SHORT).show();
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

                                    groupReference.child(currentGroupId).child("members").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            for (DataSnapshot ds: dataSnapshot.getChildren()) {

                                                String name = ds.child("name").getValue().toString();

                                                if (name.equals(enteredName)) {

                                                    final String userId = ds.getKey();

                                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                                                    reference.child("group_id").setValue("none").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()) {
                                                                DatabaseReference deleteReference = FirebaseDatabase.getInstance().getReference().child("Group")
                                                                        .child(currentGroupId).child("members").child(userId);

                                                                deleteReference.removeValue();
                                                                materialDialog.dismiss();
                                                                Toast.makeText(getActivity(), "Member deleted successfully!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

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
