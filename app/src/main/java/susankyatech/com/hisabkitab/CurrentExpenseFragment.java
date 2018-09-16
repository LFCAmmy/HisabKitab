package susankyatech.com.hisabkitab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class CurrentExpenseFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.fab)
    FloatingActionButton addExpense;

    @BindView(R.id.current_expense_spinner)
    Spinner mSpinner;

    private EditText expenseTitle, expenseAmount;
    private Calendar calender;

    private List<String> userList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference userReference, groupReference, expenseReference, userListReference;

    private int day, month, year, totalAmt;

    private String currentUserId, currentUserName, currentGroupId, monthName, date;

    private View mView;

    public CurrentExpenseFragment() { }

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

        mSpinner.setOnItemSelectedListener(this);

        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.MONTH, 0);

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);

        Log.d("TAG","" + startDate);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(mView, R.id.calendarView)
                .range(startDate, currentDate)
                .datesNumberOnScreen(5)
                .defaultSelectedDate(currentDate)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                int day = date.getTime().getDate();
                int year = date.getTime().getYear();
                int month = date.getTime().getMonth();
                getMonthName(month);

                Toast.makeText(getContext(), day+"-"+monthName+"-"+year, Toast.LENGTH_SHORT).show();

            }
        });

        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        expenseReference = FirebaseDatabase.getInstance().getReference().child("Expenses");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue().toString();
                    currentUserName = dataSnapshot.child("user_name").getValue().toString();

                    userListReference = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId).child("members");
                    userListReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                String name = ds.child("name").getValue().toString();
                                userList.add(name);
                            }

                            userList.add("All Members");
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, userList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mSpinner.setAdapter(adapter);
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
                        .customView(R.layout.add_expense,true)
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

    private String getMonthName(int month) {
        if (month == 1){
            monthName = "January";
        } else if (month == 2){
            monthName = "February";
        } else if (month == 3){
            monthName = "March";
        } else if (month == 4){
            monthName = "April";
        } else if (month == 5){
            monthName = "May";
        } else if (month == 6){
            monthName = "June";
        } else if (month == 7){
            monthName = "July";
        } else if (month == 8){
            monthName = "August";
        } else if (month == 9){
            monthName = "September";
        } else if (month == 10){
            monthName = "October";
        } else if (month == 11){
            monthName = "November";
        } else{
            monthName = "December";
        }
        return monthName;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        String item = adapterView.getItemAtPosition(i).toString();

    }

    public void onNothingSelected(AdapterView<?> arg0) { }

    private void addExpenseToDB() {

        calender =  Calendar.getInstance();
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

        if (TextUtils.isEmpty(title)){
            expenseTitle.setError("Please enter expense expenseTitle!");
            expenseTitle.requestFocus();
        }else if (TextUtils.isEmpty(amt)){
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
                            if (task.isSuccessful()){
                                HashMap userExpenseMap = new HashMap();
                                userExpenseMap.put("amount", amount);
                                expenseReference.child(currentGroupId).child(date).child(currentUserId).child("expense")
                                        .child(title).updateChildren(userExpenseMap)
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
}
