package com.susankya.abhinav.myrealmapplication.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.susankya.abhinav.myrealmapplication.CurrentExpensesUserDataModel;
import com.susankya.abhinav.myrealmapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class CurrentExpenses extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String string = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random();

    private EditText expenseTitle, expenseAmount;
    private Spinner mSpinner;
    private FloatingActionButton addExpense;
    private RecyclerView recyclerView;
    private View progressLayout;
    ProgressBar progressBar;
    TextView progressTextView;
    private RelativeLayout noExpensesLayout;
    private Context context;
    private ArrayAdapter<String> spinnerAdapter;
    private ProgressDialog progressDialog;

    private List<String> userList = new ArrayList<>();
    private HorizontalCalendar horizontalCalendar;
    private HorizontalCalendar.Builder calenderBuilder;
    private DatabaseReference userReference, expenseReference, groupReference, totalExpenditureRef, dueHistoryRef;
    private FirebaseRecyclerAdapter adapter;

    private int totalAmount;

    private String currentUserId, currentUserName, currentGroupId, date, selectedUser, token, groupCreatedDate, latestDueDateTime;

    private View mView;

    public CurrentExpenses() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_expense, container, false);

        addExpense = view.findViewById(R.id.fab);
        mSpinner = view.findViewById(R.id.spinner);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        progressLayout = view.findViewById(R.id.progress_bar_layout);
        noExpensesLayout = view.findViewById(R.id.no_expenses_layout);
        noExpensesLayout.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(context);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        expenseReference = FirebaseDatabase.getInstance().getReference().child("Expenses");
        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");
        totalExpenditureRef = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures");
        dueHistoryRef = FirebaseDatabase.getInstance().getReference().child("Due_History");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        mSpinner.setOnItemSelectedListener(this);
        spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, userList);

        mView = view;

        try {
            init();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return mView;
    }

    private void init() throws ParseException {

        progressLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue(String.class);
                    currentUserName = dataSnapshot.child("user_name").getValue(String.class);

                    expenseReference.child(currentGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                progressLayout.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                                noExpensesLayout.setVisibility(View.GONE);

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                    for (DataSnapshot de : ds.getChildren()) {

                                        String name = de.child("name").getValue(String.class);

                                        if (!userList.contains(name)) {
                                            userList.add(name);

                                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            int selectedPosition = spinnerAdapter.getPosition(currentUserName);
                                            mSpinner.setSelection(selectedPosition);
                                            mSpinner.setAdapter(spinnerAdapter);
                                        }
                                    }
                                }

                                Calendar startDate = Calendar.getInstance();
                                Date calDate = startDate.getTime();
                                SimpleDateFormat format1 = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
                                date = format1.format(calDate);
                                showExpenses(date);
                            } else {
                                progressLayout.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                                noExpensesLayout.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    groupReference.child(currentGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                groupCreatedDate = dataSnapshot.child("group_created_date").getValue(String.class);
                                final String groupCreatedTime = dataSnapshot.child("group_created_time").getValue(String.class);

                                DatabaseReference db = dueHistoryRef.child(currentGroupId);
                                Query query = db.orderByKey().limitToLast(1);

                                latestDueDateTime = groupCreatedDate + " " + groupCreatedTime;

                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                String latestDueDate = ds.getKey();
                                                String latestDueTime = ds.child("time").getValue(String.class);

                                                latestDueDateTime = latestDueDate + " " + latestDueTime;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                calenderBuilder = new HorizontalCalendar.Builder(mView, R.id.calendarView);

                                Calendar currentDate = Calendar.getInstance();
                                currentDate.add(Calendar.MONTH, 0);
                                currentDate.add(Calendar.DATE, 0);

                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
                                Date groupDate = null;

                                try {
                                    groupDate = sdf.parse(groupCreatedDate);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                Calendar startDate = Calendar.getInstance();
                                startDate.setTime(groupDate);
                                startDate.add(groupDate.getMonth(), 0);
                                startDate.add(groupDate.getDate(), 0);

                                horizontalCalendar = calenderBuilder.range(startDate, currentDate)
                                        .datesNumberOnScreen(5)
                                        .defaultSelectedDate(currentDate)
                                        .build();

                                horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
                                    @Override
                                    public void onDateSelected(Calendar cal, int position) {
                                        Date calDate = cal.getTime();
                                        SimpleDateFormat format1 = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MaterialDialog materialDialog = new MaterialDialog.Builder(context)
                        .title("Add Expense")
                        .customView(R.layout.add_expense_dialog_layout, true)
                        .positiveText("Cancel")
                        .negativeText("Add Expense")
                        .positiveColor(getResources().getColor(R.color.red))
                        .negativeColor(getResources().getColor(R.color.green))
                        .show();

                expenseTitle = materialDialog.getCustomView().findViewById(R.id.add_expense_title);
                expenseAmount = materialDialog.getCustomView().findViewById(R.id.add_expense_amount);

                materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addExpenseToDB(materialDialog);
                    }
                });
            }
        });
    }

    private void showExpenses(final String date) {

        progressLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    expenseReference.child(currentGroupId).child(date).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {

                                progressLayout.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);

                                Log.d("Pratik", currentGroupId + " " + date + " " + selectedUser);

                                expenseReference.child(currentGroupId).child(date).child(selectedUser)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.exists()) {

                                                    String userId = dataSnapshot.getKey();
                                                    String userName = dataSnapshot.child("name").getValue(String.class);
                                                    recyclerView.setVisibility(View.VISIBLE);

                                                    Query query = FirebaseDatabase.getInstance().getReference().child("Expenses").child(currentGroupId)
                                                            .child(date).child(userId).child("products")
                                                            .limitToLast(50);

                                                    displayAllCurrentExpense(query);
                                                } else {
                                                    noExpensesLayout.setVisibility(View.VISIBLE);
                                                    recyclerView.setVisibility(View.GONE);
                                                    progressLayout.setVisibility(View.GONE);
                                                    progressBar.setVisibility(View.GONE);
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


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        selectedUser = adapterView.getSelectedItem().toString();
        showExpenses(date);
    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private void addExpenseToDB(final MaterialDialog materialDialog) {

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
            expenseTitle.setError("Please enter product name!");
            expenseTitle.requestFocus();
        } else if (TextUtils.isEmpty(amt)) {
            expenseAmount.setError("Please enter amount!");
            expenseAmount.requestFocus();
        } else if (Integer.valueOf(amt) > 10000) {
            expenseAmount.setError("Amount must not exceed than 10,000!");
            expenseAmount.requestFocus();
        } else {
            progressDialog.setTitle("Adding Product");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            final int amount = Integer.valueOf(amt);

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
            final String date = currentDate.format(callForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            final String time = currentTime.format(calForTime.getTime());

            HashMap expenseMap = new HashMap();
            expenseMap.put("name", currentUserName);
            expenseReference.child(currentGroupId).child(date).child(currentUserId).updateChildren(expenseMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful()) {

                                token = generateGroupToken();
                                HashMap userExpenseMap = new HashMap();
                                userExpenseMap.put("product_name", title);
                                userExpenseMap.put("amount", amount);
                                userExpenseMap.put("date", date);
                                userExpenseMap.put("time", time);
                                userExpenseMap.put("id", token);
                                expenseReference.child(currentGroupId).child(date).child(currentUserId).child("products")
                                        .child(token).updateChildren(userExpenseMap)
                                        .addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                totalExpenditureRef.child(currentGroupId).child(currentUserId)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists()) {
                                                                    String total_amount = dataSnapshot.child("total_amount").getValue().toString();
                                                                    int totalAmt = Integer.valueOf(total_amount);

                                                                    totalAmt = totalAmt + amount;
                                                                    totalExpenditureRef.child(currentGroupId).child(currentUserId).child("total_amount").setValue(totalAmt);

                                                                    Fragment fragment = new CurrentExpenses();
                                                                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                                                    transaction.replace(R.id.content_main_frame, fragment);
                                                                    transaction.addToBackStack(null);
                                                                    transaction.commit();

                                                                    progressDialog.dismiss();

                                                                    materialDialog.dismiss();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    private void displayAllCurrentExpense(final Query query) {

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
            protected void onBindViewHolder(@NonNull final CurrentExpenseViewHolder holder, int position, @NonNull final CurrentExpensesUserDataModel model) {

                holder.setProduct_name(model.getProduct_name());
                holder.setAmount(model.getAmount());

                SimpleDateFormat sdtf = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss", Locale.ENGLISH);
                Date productDate = null;
                Date latestDate = null;
                try {
                    productDate = sdtf.parse(model.getDate() + " " + model.getTime());
                    latestDate = sdtf.parse(latestDueDateTime);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (model.getId().equals(currentUserId)) {
                    holder.actionLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.actionLayout.setVisibility(View.GONE);
                }

                if (productDate.after(latestDate)) {
                    holder.actionLayout.setVisibility(View.VISIBLE);

                } else if (productDate.before(latestDate)) {
                    holder.actionLayout.setVisibility(View.GONE);
                } else {
                    holder.actionLayout.setVisibility(View.GONE);
                }
                holder.deleteProduct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        totalExpenditureRef.child(currentGroupId).child(currentUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            totalAmount = Integer.valueOf(dataSnapshot.child("total_amount").getValue().toString());
                                            totalAmount = totalAmount - model.getAmount();
                                            totalExpenditureRef.child(currentGroupId).child(currentUserId).child("total_amount").setValue(totalAmount)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                expenseReference.child(currentGroupId).child(model.getDate()).child(currentUserId)
                                                                        .child("products").child(model.getId()).removeValue();
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
                });

                holder.editProduct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
                                .title("Update Members")
                                .customView(R.layout.edit_product_layout, true)
                                .positiveText("Save")
                                .negativeText("Cancel")
                                .positiveColor(getResources().getColor(R.color.green))
                                .negativeColor(getResources().getColor(R.color.red))
                                .canceledOnTouchOutside(true)
                                .show();

                        View customView = materialDialog.getCustomView();
                        final EditText productName = customView.findViewById(R.id.product_edit_name);
                        final EditText productAmount = customView.findViewById(R.id.product_edit_amount);

                        productAmount.setText(String.valueOf(model.getAmount()));
                        productName.setText(model.getProduct_name());

                        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final String editProductName = productName.getText().toString();
                                final String editProductAmount = productAmount.getText().toString();

                                totalExpenditureRef.child(currentGroupId).child(currentUserId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    totalAmount = Integer.valueOf(dataSnapshot.child("total_amount").getValue().toString());
                                                    totalAmount = totalAmount - model.getAmount();
                                                    totalExpenditureRef.child(currentGroupId).child(currentUserId).child("total_amount").setValue(totalAmount)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        HashMap productDetailMap = new HashMap();
                                                                        productDetailMap.put("product_name", editProductName);
                                                                        productDetailMap.put("amount", Integer.valueOf(editProductAmount));
                                                                        expenseReference.child(currentGroupId).child(model.getDate()).child(currentUserId)
                                                                                .child("products").child(model.getId()).updateChildren(productDetailMap)
                                                                                .addOnCompleteListener(new OnCompleteListener() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            totalAmount = totalAmount + Integer.valueOf(editProductAmount);
                                                                                            totalExpenditureRef.child(currentGroupId).child(currentUserId).child("total_amount").setValue(totalAmount)
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                materialDialog.dismiss();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
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
                        });
                    }

                });
            }

        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    public static class CurrentExpenseViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView editProduct, deleteProduct;
        RelativeLayout actionLayout;

        public CurrentExpenseViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            editProduct = mView.findViewById(R.id.all_current_expense_edit);
            deleteProduct = mView.findViewById(R.id.all_current_expense_delete);
            actionLayout = mView.findViewById(R.id.all_current_expense_action);

        }

        public void setAmount(int amount) {
            TextView expAmt = mView.findViewById(R.id.all_current_expense_product_price);
            String expenseAmount = String.valueOf(amount);
            expAmt.setText(expenseAmount);
        }

        public void setProduct_name(String product_name) {
            TextView extName = mView.findViewById(R.id.all_current_expense_product_name);
            extName.setText(product_name);
        }
    }

    public String generateGroupToken() {

        StringBuilder token = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            token.append(string.charAt(random.nextInt(string.length())));
        }
        return token.toString();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }
}