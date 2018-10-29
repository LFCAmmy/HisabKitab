package com.susankya.abhinav.myrealmapplication.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
import com.susankya.abhinav.myrealmapplication.R;
import com.susankya.abhinav.myrealmapplication.UserDataModel;

public class UpdateGroupMembers extends Fragment {

    private RecyclerView recyclerView;
    private Context context;

    private String currentGroupId;

    public UpdateGroupMembers() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_group_members, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        final DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference().child("Group");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue(String.class);

                    groupReference.child(currentGroupId).child("members").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                Query query = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId).child("members").limitToLast(50);
                                getAllMembers(query);
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

        return view;
    }

    private void getAllMembers(Query query) {

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

        FirebaseRecyclerOptions<UserDataModel> options = new FirebaseRecyclerOptions.Builder<UserDataModel>().setQuery(query, UserDataModel.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<UserDataModel, UpdateGroupMembersViewHolder>(options) {

            @Override
            public UpdateGroupMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.update_group_members_recycler_view_layout, parent, false);
                return new UpdateGroupMembersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UpdateGroupMembersViewHolder holder, int position, @NonNull final UserDataModel model) {

                holder.setName(model.getName());

                holder.deleteMemberBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {

                        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                                .title("Warning!")
                                .customView(R.layout.delete_member_dialog_layout, true)
                                .negativeText("Remove")
                                .positiveText("Cancel")
                                .negativeColor(getResources().getColor(R.color.red))
                                .positiveColor(getResources().getColor(R.color.green))
                                .canceledOnTouchOutside(false)
                                .show();

                        dialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {

                                final DatabaseReference groupMemberReference = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId)
                                        .child("members").child(model.getUser_id());

                                groupMemberReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {
                                            String role = dataSnapshot.child("role").getValue(String.class);

                                            if (role.equals("member")) {

                                                final DatabaseReference expendituresReference = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures")
                                                        .child(currentGroupId).child(model.getUser_id());

                                                expendituresReference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                        if (dataSnapshot.exists()) {
                                                            String amount = dataSnapshot.child("total_amount").getValue().toString();

                                                            if (amount.equals("0")) {

                                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                                                        .child(model.getUser_id());

                                                                reference.child("group_id").setValue("none").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {
                                                                            expendituresReference.removeValue();

                                                                            groupMemberReference.removeValue();

                                                                            dialog.dismiss();

                                                                            Snackbar.make(view, "Group member deleted successfully!",
                                                                                    Snackbar.LENGTH_LONG).show();
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                dialog.dismiss();

                                                                Snackbar.make(view, model.getName() + "'s" + " due must be cleared before you can remove " +
                                                                        " him/her from the group!", Snackbar.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            } else {
                                                dialog.dismiss();
                                                Snackbar.make(view, "You cannot delete yourself because your are the admin of this group!",
                                                        Snackbar.LENGTH_LONG).show();
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
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    public static class UpdateGroupMembersViewHolder extends RecyclerView.ViewHolder {

        Button deleteMemberBtn;
        View mView;

        public UpdateGroupMembersViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            deleteMemberBtn = mView.findViewById(R.id.delete_member_btn);
        }

        public void setName(String name) {
            TextView displayUserName_TV = mView.findViewById(R.id.user_name_tv);
            displayUserName_TV.setText(name);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }
}
