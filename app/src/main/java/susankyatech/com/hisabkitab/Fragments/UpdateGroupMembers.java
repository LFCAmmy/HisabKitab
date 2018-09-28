package susankyatech.com.hisabkitab.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private RecyclerView recyclerView;

    private DatabaseReference groupReference;

    private String currentGroupId;

    public UpdateGroupMembers() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_group_members, container, false);

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
            protected void onBindViewHolder(@NonNull UpdateGroupMembersViewHolder holder, int position, @NonNull final UserDataModel model) {
                holder.setName(model.getName());

                holder.deleteMemberBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                                .title("Warning!")
                                .customView(R.layout.delete_account_dialog_layout, true)
                                .negativeText("Cancel")
                                .positiveText("Remove")
                                .negativeColor(getResources().getColor(R.color.green))
                                .positiveColor(getResources().getColor(R.color.red))
                                .canceledOnTouchOutside(false)
                                .show();

                        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                final DatabaseReference expendituresReference = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures")
                                        .child(currentGroupId).child(model.getUser_id());

                                expendituresReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        String amount = dataSnapshot.child("total_amount").getValue().toString();

                                        if (amount.equals("0")) {

                                            expendituresReference.removeValue();

                                            DatabaseReference groupMemberDeleteReference = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId)
                                                    .child("members").child(model.getUser_id());

                                            groupMemberDeleteReference.removeValue();

                                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(model.getUser_id())
                                                    .child("group_id");

                                            reference.setValue("none").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getActivity(), "Group member deleted successfully!", Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(getActivity(), model.getName() + "'s" + " due must be cleared before you can remove the account from the " +
                                                            "group!", Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
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
            deleteMemberBtn.setText(R.string.remove);
        }

        public void setName(String name) {
            TextView displayUserName_TV = mView.findViewById(R.id.user_name_tv);
            displayUserName_TV.setText(name);
        }
    }
}
