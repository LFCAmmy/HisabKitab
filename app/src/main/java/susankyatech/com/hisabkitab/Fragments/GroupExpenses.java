package susankyatech.com.hisabkitab.Fragments;

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
import android.widget.ProgressBar;
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

    private RecyclerView recyclerView;

    private DatabaseReference groupReference, userReference, totalExpenseRef, dueHistoryRef;

    private String currentGroupId;
    private int totalExpenses;
    private long memberCount, dueAmount;
    private List<DueAmount> userExpenses = new ArrayList<>();
    private List<DueHistoryDataModel> dueHistoryList = new ArrayList<>();

    private TextView totalAmountTV, eachAmountTV, dueDateTV;
    private View progressLayout;
    ProgressBar progressBar;
    TextView progressTextView;
    Button clearDueBtn;

    public GroupExpenses() {

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
            }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_expenses, container, false);


        clearDueBtn = view.findViewById(R.id.clear_due_btn);
        recyclerView = view.findViewById(R.id.recycler_view);
        totalAmountTV = view.findViewById(R.id.group_exp_total_amt);
        eachAmountTV = view.findViewById(R.id.group_exp_each_amt);
        dueDateTV = view.findViewById(R.id.group_exp_due_date);
        progressLayout = view.findViewById(R.id.progressBarLayout);
        progressBar = view.findViewById(R.id.progressBar);
        progressTextView = view.findViewById(R.id.progressTV);

        init();


        return view;
    }

    private void init() {
        progressLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");
        totalExpenseRef = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures");
        dueHistoryRef = FirebaseDatabase.getInstance().getReference().child("Due_History");

        userReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentGroupId = dataSnapshot.child("group_id").getValue().toString();
                groupReference.child(currentGroupId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String groupDate = dataSnapshot.child("group_created_date").getValue().toString();
                            groupReference.child(currentGroupId).child("members")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                memberCount = dataSnapshot.getChildrenCount();
                                                totalExpenseRef.child(currentGroupId).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            progressLayout.setVisibility(View.GONE);
                                                            progressBar.setVisibility(View.GONE);
                                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                                String userId = ds.getKey();
                                                                int userAmount = Integer.valueOf(ds.child("total_amount").getValue().toString());
                                                                totalExpenses += userAmount;
                                                                Log.d(TAG, "onDataChange: "+ userAmount);

                                                                userExpenses.add(new DueAmount(userId, userAmount));
                                                                Log.d(TAG, "onDataChange: "+userExpenses.size());

                                                                Query query = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId).child("members").limitToLast(50);
                                                                getAllUserName(query);
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

                            dueHistoryRef.child(currentGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        DatabaseReference db = dueHistoryRef.child(currentGroupId);
                                        Query query = db.orderByKey().limitToLast(1);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                                    String latestDate = ds.getKey();
                                                    dueDateTV.setText(latestDate);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }else {

                                        dueDateTV.setText(groupDate);
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
                clearExpenditureDue();
            }
        });
    }

    private void clearExpenditureDue() {
        progressLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    progressLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    currentGroupId = dataSnapshot.child("group_id").getValue().toString();
                    totalExpenseRef.child(currentGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    long count = ds.getChildrenCount();
                                    Log.d(TAG, "onDataChange: "+ count);
                                    String userId = ds.getKey();
                                    String userName = ds.child("name").getValue().toString();
                                    int userAmount = Integer.valueOf(ds.child("total_amount").getValue().toString());
                                    Log.d(TAG, "onDataChange: "+ userName);

                                    dueHistoryList.add(new DueHistoryDataModel(userId, userName, userAmount));
                                    Log.d(TAG, "onDataChange: "+dueHistoryList.size());

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
        Log.d("asdasd", "addToDueHistory: "+dueHistoryList.size());
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String date = currentDate.format(callForDate.getTime());

        dueHistoryRef.child(currentGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild(date)){
                        Toast.makeText(getContext(), "Due has already been cleared on " + date + "!", Toast.LENGTH_SHORT).show();
                    } else {
                        for (int i = 0; i < dueHistoryList.size(); i++) {
                            HashMap historyMap = new HashMap();
                            historyMap.put("user_id", dueHistoryList.get(i).userId);
                            historyMap.put("name", dueHistoryList.get(i).userName);
                            historyMap.put("total_amount", dueHistoryList.get(i).dueAmount);
                            dueHistoryRef.child(currentGroupId).child(date).child(dueHistoryList.get(i).userId).updateChildren(historyMap)
                                    .addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if (task.isSuccessful()) {
                                                totalExpenseRef.child(currentGroupId).child(userId).child("total_amount").setValue(0)
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        String totalAmount = String.valueOf(totalExpenses);
        totalAmountTV.setText(totalAmount);

        long each = totalExpenses / memberCount;
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
                        dueAmount = userExpenses.get(i).dueAmount - (totalExpenses / memberCount);
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



