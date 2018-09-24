package susankyatech.com.hisabkitab.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import susankyatech.com.hisabkitab.DueAmount;
import susankyatech.com.hisabkitab.R;
import susankyatech.com.hisabkitab.UserDataModel;

import static android.content.ContentValues.TAG;

public class GroupExpenses extends Fragment {

    private RecyclerView recyclerView;

    private DatabaseReference groupReference, userReference, totalExpenseRef;

    private String currentGroupId;
    private int totalExpenses;
    private long memberCount, dueAmount;
    private List<DueAmount> userExpenses = new ArrayList<>();

    public GroupExpenses() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_expenses, container, false);

        Button clearDueBtn = view.findViewById(R.id.clear_due_btn);
        recyclerView = view.findViewById(R.id.recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");
        totalExpenseRef = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentGroupId = dataSnapshot.child("group_id").getValue().toString();
                groupReference.child(currentGroupId).child("members").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            memberCount = dataSnapshot.getChildrenCount();

                            totalExpenseRef.child(currentGroupId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String userId = ds.getKey();
                                            int userAmount = Integer.valueOf(ds.child("total_amount").getValue().toString());
                                            totalExpenses += userAmount;

                                            userExpenses.add(new DueAmount(userId, userAmount));

                                            Query query = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId).child("members").limitToLast(50);
                                            getAllUserName(query);
                                        }
                                        Log.d("asd", "onDataChange: " + totalExpenses);
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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        clearDueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Due cleared!!", Toast.LENGTH_SHORT).show();
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

        FirebaseRecyclerOptions<UserDataModel> options = new FirebaseRecyclerOptions.Builder<UserDataModel>().setQuery(query, UserDataModel.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<UserDataModel, GroupExpensesViewHolder>(options) {

            @Override
            public GroupExpensesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_expenses_recycler_view_layout, parent, false);
                return new GroupExpensesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull GroupExpensesViewHolder holder, int position, @NonNull UserDataModel model) {
                holder.setName(model.getName());

//                if (userExpenses.get(position).userId.equals(model.getUser_id())){
//                    dueAmount = userExpenses.get(position).dueAmount - (totalExpenses/memberCount);
//                    holder.setAmount(dueAmount);
//                } else {
//                    dueAmount = 0 - (totalExpenses/memberCount);
//                    holder.setAmount(dueAmount);
//                }

                for (int i = 0; i < userExpenses.size(); i++) {
                    Log.d("asd", "onBindViewHolder: " + userExpenses.get(i));
                    if (userExpenses.get(i).userId.equals(model.getUser_id())) {
                        dueAmount = userExpenses.get(i).dueAmount - (totalExpenses / memberCount);
                        holder.setAmount(dueAmount);
                    }
                }


            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public static class GroupExpensesViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public GroupExpensesViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setAmount(long amount) {
            TextView displayDueAmount = mView.findViewById(R.id.group_expenses_due_amount_tv);
            String dueAmount = String.valueOf(amount);
            displayDueAmount.setText(dueAmount);
        }

        public void setName(String name) {
            TextView displayUserName_TV = mView.findViewById(R.id.group_expenses_user_name_tv);
            displayUserName_TV.setText(name);
        }
    }
}



