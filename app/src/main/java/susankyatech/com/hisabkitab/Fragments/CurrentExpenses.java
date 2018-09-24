package susankyatech.com.hisabkitab.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import susankyatech.com.hisabkitab.CurrentExpensesUserDataModel;
import susankyatech.com.hisabkitab.R;

public class CurrentExpenses extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String string = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random();

    private EditText expenseTitle, expenseAmount;
    private Spinner mSpinner;
    private FloatingActionButton addExpense;
    private RecyclerView recyclerView;

    private List<String> userList = new ArrayList<>();

    private DatabaseReference expenseReference, userListReference;
    private FirebaseRecyclerAdapter adapter;

    private int totalAmount;

    private String currentUserId, currentUserName, currentGroupId, date, selectedUser, token, groupCreatedDate;

    private View mView;

    public CurrentExpenses() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_expense, container, false);

        addExpense = view.findViewById(R.id.fab);
        mSpinner = view.findViewById(R.id.spinner);
        recyclerView = view.findViewById(R.id.recycler_view);

        mView = view;

        try {
            init();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return mView;
    }

    private void init() throws ParseException {

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        expenseReference = FirebaseDatabase.getInstance().getReference().child("Expenses");
        userListReference = FirebaseDatabase.getInstance().getReference().child("Group");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        mSpinner.setOnItemSelectedListener(this);
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, userList);

//        String easy = RandomString.digits + "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";
//        RandomString tickets = new RandomString(6, new SecureRandom(), easy);
//
//        token = tickets.nextString();


        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue().toString();
                    currentUserName = dataSnapshot.child("user_name").getValue().toString();

                    userListReference.child(currentGroupId).child("members")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String name = ds.child("name").getValue().toString();
                                        userList.add(name);
                                    }

                                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    mSpinner.setAdapter(spinnerAdapter);

                                    Calendar startDate = Calendar.getInstance();
                                    Date calDate = startDate.getTime();
                                    SimpleDateFormat format1 = new SimpleDateFormat("dd-MMMM-yyyy");
                                    date = format1.format(calDate);
                                    showExpenses(date);

                                    userListReference.child(currentGroupId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                groupCreatedDate = dataSnapshot.child("group_created_date").getValue().toString();

                                                Calendar currentDate = Calendar.getInstance();
                                                currentDate.add(Calendar.MONTH, 0);
                                                currentDate.add(Calendar.DATE, 0);

                                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy");
                                                Date groupDate = null;
                                                try {
                                                    groupDate = sdf.parse(groupCreatedDate);
                                                    Log.d("asd", "onDataChange: "+groupDate);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                                Calendar startDate = Calendar.getInstance();
                                                startDate.setTime(groupDate);
                                                startDate.add(groupDate.getMonth(),0);
                                                startDate.add(groupDate.getDate(),0);

                                                HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(mView, R.id.calendarView)
                                                        .range(startDate, currentDate)
                                                        .datesNumberOnScreen(5)
                                                        .defaultSelectedDate(currentDate)
                                                        .build();


                                                horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
                                                    @Override
                                                    public void onDateSelected(Calendar cal, int position) {
                                                        Date calDate = cal.getTime();
                                                        SimpleDateFormat format1 = new SimpleDateFormat("dd-MMMM-yyyy");
                                                        date = format1.format(calDate);

                                                        showExpenses(date);

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

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                        .title("Add Expense")
                        .customView(R.layout.add_expense_dialog_layout, true)
                        .positiveText("Add Expense")
                        .negativeText("Close")
                        .positiveColor(getResources().getColor(R.color.green))
                        .negativeColor(getResources().getColor(R.color.red))
                        .show();

                expenseTitle = materialDialog.getCustomView().findViewById(R.id.add_expense_title);
                expenseAmount = materialDialog.getCustomView().findViewById(R.id.add_expense_amount);

                materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addExpenseToDB();
                        materialDialog.dismiss();
                    }
                });
                materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        materialDialog.dismiss();
                    }
                });
            }
        });

    }

    private void showExpenses(final String date) {

        expenseReference.child(currentGroupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String expenseDate = ds.getKey();

                        for (DataSnapshot de : ds.getChildren()) {

                            String userId = de.getKey();
                            String userName = de.child("name").getValue().toString();

                            if (expenseDate.equals(date)) {
                                if (userName.equals(selectedUser)) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    Query query = FirebaseDatabase.getInstance().getReference().child("Expenses").child(currentGroupId)
                                            .child(expenseDate).child(userId).child("products")
                                            .limitToLast(50);

                                    displayAllCurrentExpense(query);
                                }
                            } else {
                                recyclerView.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        selectedUser = adapterView.getSelectedItem().toString();
        showExpenses(date);
    }

    public void onNothingSelected(AdapterView<?> arg0) {}

    private void addExpenseToDB() {

        Calendar calender = Calendar.getInstance();
        int day = calender.get(Calendar.DAY_OF_MONTH);
        int month = calender.get(Calendar.MONTH);
        int year = calender.get(Calendar.YEAR);
        month = month + 1;

        String mYear = String.valueOf(year);
        String mMonth = String.valueOf(month);
        String mDay = String.valueOf(day);

        date = mDay + "/" + mMonth + "/" + mYear;

        final String title = expenseTitle.getText().toString();
        final String amt = expenseAmount.getText().toString();

        if (TextUtils.isEmpty(title)) {
            expenseTitle.setError("Please enter expense expenseTitle!");
            expenseTitle.requestFocus();
        } else if (TextUtils.isEmpty(amt)) {
            expenseAmount.setError("Please enter expense expenseAmount!");
            expenseAmount.requestFocus();
        } else {
            final int amount = Integer.valueOf(amt);

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String date = currentDate.format(callForDate.getTime());

            HashMap expenseMap = new HashMap();
            expenseMap.put("name", currentUserName);
            expenseMap.put("total_amount", totalAmount);
            expenseReference.child(currentGroupId).child(date).child(currentUserId).updateChildren(expenseMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful()) {
                                HashMap userExpenseMap = new HashMap();
                                userExpenseMap.put("product_name", title);
                                userExpenseMap.put("amount", amount);
                                token = generateGroupToken();
                                expenseReference.child(currentGroupId).child(date).child(currentUserId).child("products")
                                        .child(token).updateChildren(userExpenseMap)
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                totalAmount = totalAmount + amount;
                                                expenseReference.child(currentGroupId).child(date).child(currentUserId)
                                                        .child("total_amount").setValue(totalAmount);
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    private void displayAllCurrentExpense(Query query) {

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

        FirebaseRecyclerOptions<CurrentExpensesUserDataModel> options = new FirebaseRecyclerOptions.Builder<CurrentExpensesUserDataModel>()
                .setQuery(query, CurrentExpensesUserDataModel.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<CurrentExpensesUserDataModel, CurrentExpenseViewHolder>(options) {

            @NonNull
            @Override
            public CurrentExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_expense_recycler_view_layout, parent, false);
                return new CurrentExpenseViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CurrentExpenseViewHolder holder, int position, @NonNull CurrentExpensesUserDataModel model) {
                holder.setProduct_name(model.getProduct_name());
                holder.setAmount(model.getAmount());
            }

        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    public static class CurrentExpenseViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public CurrentExpenseViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setAmount(int amount){
            TextView expAmt = mView.findViewById(R.id.all_current_expense_product_price);
            String expenseAmount = String.valueOf(amount);
            expAmt.setText(expenseAmount);
        }

        public void setProduct_name(String product_name){
            TextView extName = mView.findViewById(R.id.all_current_expense_product_name);
            extName.setText(product_name);
        }
    }

    public String generateGroupToken() {

        StringBuilder token = new StringBuilder(6);
        for (int i=0; i<6; i++) {
            token.append(string.charAt(random.nextInt(string.length())));
        }
        return token.toString();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (adapter != null)
        {
            adapter.startListening();
        }
    }
}
