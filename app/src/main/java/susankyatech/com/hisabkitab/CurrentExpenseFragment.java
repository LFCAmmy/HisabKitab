package susankyatech.com.hisabkitab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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

    private int day, month, year;

    private String currentUserId, currentUserName, currentGroupId, date;

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

        Calendar mDate = Calendar.getInstance();
        currentDate.add(Calendar.MONTH, -1);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(mView, R.id.calendarView)
                .range(mDate,currentDate)
                .datesNumberOnScreen(5)
                .defaultSelectedDate(currentDate)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {


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
//                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, userList);
//                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                            mSpinner.setAdapter(adapter);
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
        final String amount = expenseAmount.getText().toString();

        if (TextUtils.isEmpty(title)){
            expenseTitle.setError("Please enter expense expenseTitle!");
            expenseTitle.requestFocus();
        }else if (TextUtils.isEmpty(amount)){
            expenseAmount.setError("Please enter expense expenseAmount!");
            expenseAmount.requestFocus();
        } else {
            HashMap expenseMap = new HashMap();
            expenseMap.put("name", currentUserName);
            expenseReference.child(currentGroupId).child(currentUserId).updateChildren(expenseMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                HashMap userExpenseMap = new HashMap();
                                userExpenseMap.put("expenseTitle", title);
                                userExpenseMap.put("expenseAmount", amount);
                            }
                        }
                    });
        }
    }
}
