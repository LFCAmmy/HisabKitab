package susankyatech.com.hisabkitab.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import susankyatech.com.hisabkitab.DueAmount;
import susankyatech.com.hisabkitab.DueHistoryDataModel;
import susankyatech.com.hisabkitab.R;
import susankyatech.com.hisabkitab.UserDataModel;

import static android.content.ContentValues.TAG;

public class GroupExpenses extends Fragment {

    private TextView dueDateSinceTV, totalAmountTV, eachAmountTV;
    private Button clearDueBtn;
    private RecyclerView recyclerView;
    private RelativeLayout progressBarLayout;

    private DatabaseReference userReference, groupReference, totalExpendituresReference, dueHistoryReference;

    private String currentGroupId, groupCreatedDate, userState, userId;
    private int totalExpenses = 0, userAmount;
    private long groupMembersCount, dueAmount;

    private List<DueAmount> userExpenses = new ArrayList<>();
    private List<DueHistoryDataModel> dueHistoryList = new ArrayList<>();

    public GroupExpenses() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_expenses, container, false);

        progressBarLayout = view.findViewById(R.id.progress_bar_layout);
        dueDateSinceTV = view.findViewById(R.id.due_date_since_tv);
        totalAmountTV = view.findViewById(R.id.total_amount_tv);
        eachAmountTV = view.findViewById(R.id.each_amount_tv);
        recyclerView = view.findViewById(R.id.recycler_view);
        clearDueBtn = view.findViewById(R.id.clear_due_btn);

        progressBarLayout.setVisibility(View.VISIBLE);
        clearDueBtn.setVisibility(View.INVISIBLE);

        SharedPreferences sp = getActivity().getSharedPreferences("UserInfo", 0);
        userState = sp.getString("role", "none");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");
        totalExpendituresReference = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures");
        dueHistoryReference = FirebaseDatabase.getInstance().getReference().child("Due_History");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue().toString();

                    groupReference.child(currentGroupId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                groupCreatedDate = dataSnapshot.child("group_created_date").getValue().toString();

                                groupReference.child(currentGroupId).child("members").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {
                                            groupMembersCount = dataSnapshot.getChildrenCount();

                                            totalExpendituresReference.child(currentGroupId).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    if (dataSnapshot.exists()) {
                                                        progressBarLayout.setVisibility(View.GONE);

                                                        if (userState.equals("admin")) {

                                                            clearDueBtn.setVisibility(View.VISIBLE);

                                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                                userId = ds.getKey();
                                                                userAmount = Integer.valueOf(ds.child("total_amount").getValue().toString());
                                                                totalExpenses += userAmount;

                                                                userExpenses.add(new DueAmount(userId, userAmount));

                                                                Query query = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId).child("members")
                                                                        .limitToLast(25);
                                                                getAllMembers(query);
                                                            }
                                                        } else {
                                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                                userId = ds.getKey();
                                                                userAmount = Integer.valueOf(ds.child("total_amount").getValue().toString());
                                                                totalExpenses += userAmount;

                                                                userExpenses.add(new DueAmount(userId, userAmount));

                                                                Query query = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId)
                                                                        .child("members").limitToLast(25);
                                                                getAllMembers(query);
                                                            }
                                                        }
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

                                dueHistoryReference.child(currentGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            DatabaseReference db = dueHistoryReference.child(currentGroupId);
                                            Query query = db.orderByKey().limitToLast(1);

                                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                        String latestDate = ds.getKey();
                                                        dueDateSinceTV.setText(latestDate);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        } else {
                                            dueDateSinceTV.setText(groupCreatedDate);
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



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        clearDueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearExpenditureDue();
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.group_expenses_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_due_history: {
                Fragment fragment = new susankyatech.com.hisabkitab.Fragments.DueHistory();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.content_main_frame, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            }
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void clearExpenditureDue() {

        progressBarLayout.setVisibility(View.VISIBLE);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    progressBarLayout.setVisibility(View.GONE);

                    currentGroupId = dataSnapshot.child("group_id").getValue().toString();

                    totalExpendituresReference.child(currentGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    long count = ds.getChildrenCount();
                                    Log.d("tyu", "onDataChange: "+count);

                                    String userId = ds.getKey();
                                    String userName = ds.child("name").getValue().toString();
                                    int userAmount = Integer.valueOf(ds.child("total_amount").getValue().toString());

                                    dueHistoryList.add(new DueHistoryDataModel(userId, userName, userAmount));

                                    addToDueHistory(userId);
                                }
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

    private void addToDueHistory(final String userId) {

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String date = currentDate.format(callForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        final String time = currentTime.format(calForTime.getTime());

        dueHistoryReference.child(currentGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild(date)){
                        Toast.makeText(getContext(), "Due has already been cleared on " + date + "!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    dueHistoryReference.child(currentGroupId).child(date).child("time").setValue(time)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isComplete()){
                                        for (int i = 0; i < dueHistoryList.size(); i++) {
                                            HashMap historyMap = new HashMap();
                                            historyMap.put("user_id", dueHistoryList.get(i).userId);
                                            historyMap.put("name", dueHistoryList.get(i).userName);
                                            historyMap.put("total_amount", dueHistoryList.get(i).dueAmount);
                                            dueHistoryReference.child(currentGroupId).child(date).child("members").child(dueHistoryList.get(i).userId).updateChildren(historyMap)
                                                    .addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if (task.isSuccessful()) {
                                                                totalExpendituresReference.child(currentGroupId).child(userId).child("total_amount").setValue(0)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    Fragment fragment = new GroupExpenses();
                                                                                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                                                                    transaction.replace(R.id.content_main_frame, fragment);
                                                                                    transaction.addToBackStack(null);
                                                                                    transaction.commit();
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        String totalAmount = String.valueOf(totalExpenses);
        totalAmountTV.setText(totalAmount);

        long each = totalExpenses / groupMembersCount;
        String eachAmount = String.valueOf(each);
        eachAmountTV.setText(eachAmount);


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

                for (int i = 0; i < userExpenses.size(); i++) {
                    if (userExpenses.get(i).userId.equals(model.getUser_id())) {
                        dueAmount = userExpenses.get(i).dueAmount - (totalExpenses / groupMembersCount);
                        holder.setAmount(dueAmount);
                        holder.setTotalSpentAmount(userExpenses.get(i).dueAmount);
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

        public void setTotalSpentAmount(int amount) {
            TextView displayTotalSpent = mView.findViewById(R.id.group_expenses_each_total_spent_tv);
            String totalSpent = String.valueOf(amount);
            displayTotalSpent.setText(totalSpent);
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



