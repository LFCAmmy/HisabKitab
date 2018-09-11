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
import android.widget.EditText;

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

public class CurrentExpenseFragment extends Fragment {

    @BindView(R.id.fab)
    FloatingActionButton addExpense;

    private EditText title, amount;
    private String currentUserId, userName, groupId, date;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, expenseRef, userListRef;

    private Calendar mCalendar;
    int day, month, year;

    private List<String> userList = new ArrayList<>();

    public CurrentExpenseFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_expense, container, false);

        ButterKnife.bind(this,view);

        init();

        return view;
    }

    private void init() {

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        expenseRef = FirebaseDatabase.getInstance().getReference().child("Expenses");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("group_id")){
                        groupId = dataSnapshot.child("group_id").getValue().toString();
                    }
                    userName = dataSnapshot.child("user_name").getValue().toString();
                    Log.d("Pratik","" + userName);

                    userListRef = FirebaseDatabase.getInstance().getReference().child("Group").child(groupId).child("members");
                    userListRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                String name = ds.child("name").getValue().toString();
                                userList.add(name);
                                Log.d("asdasd", "onDataChange: "+userList);
                            }
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

                title = materialDialog.getCustomView().findViewById(R.id.add_expense_title);
                amount = materialDialog.getCustomView().findViewById(R.id.add_expense_amount);

                materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addExpenseToDB();

//                        materialDialog.dismiss();

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

    private void addExpenseToDB() {
        mCalendar =  Calendar.getInstance();

        day = mCalendar.get(Calendar.DAY_OF_MONTH);
        month = mCalendar.get(Calendar.MONTH);
        year = mCalendar.get(Calendar.YEAR);

        month = month + 1;

        String mYear = String.valueOf(year);
        String mMonth = String.valueOf(month);
        String mDay = String.valueOf(day);

        date = mDay + "/" + mMonth + "/" + mYear;

        final String expenseTitle = title.getText().toString();
        final String expenseAmount = amount.getText().toString();

        if (TextUtils.isEmpty(expenseTitle)){
            title.setError("Enter Expense Title");
            title.requestFocus();
            return;
        }else if (TextUtils.isEmpty(expenseAmount)){
            amount.setError("Enter Expense Amount");
            amount.requestFocus();
            return;
        } else {
            HashMap expenseMap = new HashMap();
            expenseMap.put("name", userName);
            expenseRef.child(groupId).child(currentUserId).updateChildren(expenseMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                HashMap userExpenseMap = new HashMap();
                                userExpenseMap.put("title", expenseTitle);
                                userExpenseMap.put("amount", expenseAmount);
                            }
                        }
                    });
        }
    }
}
