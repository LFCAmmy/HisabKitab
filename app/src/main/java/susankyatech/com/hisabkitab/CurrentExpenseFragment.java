package susankyatech.com.hisabkitab;

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

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

import static android.content.ContentValues.TAG;

public class CurrentExpenseFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.fab)
    FloatingActionButton addExpense;
    @BindView(R.id.current_expense_spinner)
    Spinner mSpinner;
    @BindView(R.id.current_expense_recycler_view)
    RecyclerView currentExpenseList;

    private EditText expenseTitle, expenseAmount;
    private Calendar calender;

    private List<String> userList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference userReference, expenseReference, userListReference;

    private int day, month, year, totalAmt;

    private String currentUserId, currentUserName, currentGroupId, date, selectedUser, token;

    private View mView;

    public CurrentExpenseFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_expense, container, false);

        ButterKnife.bind(this, view);

        mView = view;

        init();

        return mView;
    }

    private void init() {

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        expenseReference = FirebaseDatabase.getInstance().getReference().child("UserExpenses");
        userListReference = FirebaseDatabase.getInstance().getReference().child("Group");

        currentExpenseList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        currentExpenseList.setLayoutManager(linearLayoutManager);

        mSpinner.setOnItemSelectedListener(this);

        String easy = RandomString.digits + "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";
        RandomString tickets = new RandomString(6, new SecureRandom(), easy);

        token = tickets.nextString();

        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.MONTH, 0);

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);

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

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue().toString();
                    currentUserName = dataSnapshot.child("user_name").getValue().toString();


                    userListReference.child(currentGroupId).child("members")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String name = ds.child("name").getValue().toString();
                                        userList.add(name);
                                    }

                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, userList);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    mSpinner.setAdapter(adapter);

                                    Calendar startDate = Calendar.getInstance();
                                    Date calDate = startDate.getTime();
                                    SimpleDateFormat format1 = new SimpleDateFormat("dd-MMMM-yyyy");
                                    date = format1.format(calDate);
                                    showExpenses(date);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                        .title("Add Expense")
                        .customView(R.layout.add_expense, true)
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
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String expenseDate = ds.getKey();
                        for (DataSnapshot de : ds.getChildren()) {
                            String userId = de.getKey();
                            String userName = de.child("name").getValue().toString();
                            if (expenseDate.equals(date)) {
                                Log.d("armaan", "onDataChange: " + userName);
                                Log.d("armaan", "sel: " + selectedUser);
                                if (userName.equals(selectedUser)) {
                                    Log.d("armaan", "onDataChange: hiiiiiii");
                                    currentExpenseList.setVisibility(View.VISIBLE);
                                    Query query = FirebaseDatabase.getInstance()
                                            .getReference()
                                            .child("UserExpenses").child(currentGroupId).child(expenseDate).child(userId).child("products")
                                            .limitToLast(50);
                                    DatabaseReference userExpenseRef = expenseReference.child(currentGroupId).child(expenseDate).child(userId).child("products");
                                    displayAllCurrentExpense(query);

                                }

                            } else {
                                currentExpenseList.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "not matched", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        selectedUser = adapterView.getSelectedItem().toString();
        showExpenses(date);
    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private void addExpenseToDB() {

        calender = Calendar.getInstance();
        day = calender.get(Calendar.DAY_OF_MONTH);
        month = calender.get(Calendar.MONTH);
        year = calender.get(Calendar.YEAR);
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
            expenseMap.put("total_amount", totalAmt);
            expenseReference.child(currentGroupId).child(date).child(currentUserId).updateChildren(expenseMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                HashMap userExpenseMap = new HashMap();
                                userExpenseMap.put("product_name", title);
                                userExpenseMap.put("amount", amount);
                                expenseReference.child(currentGroupId).child(date).child(currentUserId).child("products")
                                        .child(token).updateChildren(userExpenseMap)
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                totalAmt = totalAmt + amount;
                                                expenseReference.child(currentGroupId).child(date).child(currentUserId)
                                                        .child("total_amount").setValue(totalAmt);
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

        FirebaseRecyclerOptions<UserExpenses> options =
                new FirebaseRecyclerOptions.Builder<UserExpenses>()
                .setQuery(query, UserExpenses.class)
                .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<UserExpenses, CurrentExpenseViewHolder>(options) {
            @NonNull
            @Override
            public CurrentExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_user_expense_layout, parent, false);
                return new CurrentExpenseViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CurrentExpenseViewHolder holder, int position, @NonNull UserExpenses model) {
                Log.d("TAG", "onBindViewHolder: "+model.getProduct_name());
                holder.setProduct_name(model.getProduct_name());
            }

        };
        adapter.startListening();
        currentExpenseList.setAdapter(adapter);
    }

    public static class CurrentExpenseViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public CurrentExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProductId(int position){
            TextView extId = mView.findViewById(R.id.all_current_expense_id);
            String id = String.valueOf(position + 1);
            extId.setText(id);


        }

        public void setAmount(int amount){
            Log.d("armaan", "setAmount: "+amount);
            TextView expAmt = mView.findViewById(R.id.all_current_expense_product_price);
            String expenseAmount = String.valueOf(amount);
            expAmt.setText(expenseAmount);
        }

        public void setProduct_name(String product_name){
            TextView extName = mView.findViewById(R.id.all_current_expense_product_name);
            extName.setText(product_name);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
