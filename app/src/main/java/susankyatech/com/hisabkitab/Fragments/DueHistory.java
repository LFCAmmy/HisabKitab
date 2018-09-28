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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import susankyatech.com.hisabkitab.DueAmount;
import susankyatech.com.hisabkitab.R;
import susankyatech.com.hisabkitab.UserDataModel;

import static android.content.ContentValues.TAG;

public class DueHistory extends Fragment implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.due_history_each_amt)
    TextView eachAmountTV;
    @BindView(R.id.due_history_total_amt)
    TextView totalAmountTV;
    @BindView(R.id.due_history_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.history_text)
    RelativeLayout historyTextLayout;
    @BindView(R.id.progressBarLayout)
    View progressLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.progressTV)
    TextView progressTextView;

    private Spinner spinner;

    private List<String> dateList = new ArrayList<>();
    private List<DueAmount> userExpenses = new ArrayList<>();

    private String currentUserId, currentGroupId, selectedDate;
    private int totalExpenses;
    private long memberCount, dueAmount;

    private View mView;

    private DatabaseReference historyReference, groupRef;

    public DueHistory() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_due_history, container, false);

        ButterKnife.bind(this, view);
        spinner = view.findViewById(R.id.spinner);

        mView = view;

        init();

        return mView;

    }

    private void init() {
        progressLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        groupRef = FirebaseDatabase.getInstance().getReference().child("Group");
        historyReference = FirebaseDatabase.getInstance().getReference().child("Due_History");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        spinner.setOnItemSelectedListener(this);
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, dateList);

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue().toString();

                    historyReference.child(currentGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String date = ds.getKey();
                                    dateList.add(date);
                                }

                                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(spinnerAdapter);
                            }else {
                                mainLayout.setVisibility(View.GONE);
                                historyTextLayout.setVisibility(View.VISIBLE);
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        progressLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        selectedDate = adapterView.getSelectedItem().toString();
        getAllHistory(selectedDate);


    }

    private void getAllHistory(final String selectedDate) {
        groupRef.child(currentGroupId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    progressLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    memberCount = dataSnapshot.getChildrenCount();
                    historyReference.child(currentGroupId).child(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                totalExpenses = 0;
                                for (DataSnapshot de : dataSnapshot.getChildren()){
                                    String userId = de.getKey();
                                    int userAmount = Integer.valueOf(de.child("total_amount").getValue().toString());

                                    totalExpenses = totalExpenses + userAmount;

                                    userExpenses.add(new DueAmount(userId, userAmount));

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

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<UserDataModel, DueHistoryViewHolder>(options) {

            @Override
            public DueHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_expenses_recycler_view_layout, parent, false);
                return new DueHistoryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull DueHistoryViewHolder holder, int position, @NonNull UserDataModel model) {
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
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static class DueHistoryViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public DueHistoryViewHolder(@NonNull View itemView) {
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

