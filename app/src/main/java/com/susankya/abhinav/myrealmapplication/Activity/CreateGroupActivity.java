package com.susankya.abhinav.myrealmapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.susankya.abhinav.myrealmapplication.General.Keys;
import com.susankya.abhinav.myrealmapplication.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class CreateGroupActivity extends AppCompatActivity {

    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        final EditText groupNameET = findViewById(R.id.group_name_et);
        final EditText groupMaxMembersET = findViewById(R.id.group_max_members_et);
        Button createGroupBtn = findViewById(R.id.create_group_btn);

        final ProgressDialog loadingBar = new ProgressDialog(this);

        final SharedPreferences sp = getSharedPreferences(Keys.PREFERENCES, 0);

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        final DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference().child("Group");
        final DatabaseReference totalExpenditureReference = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("user_name").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                String groupName = groupNameET.getText().toString();
                String groupMaxMembers = groupMaxMembersET.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    groupNameET.setError("Please enter a group name!");
                    groupNameET.requestFocus();
                } else if (groupName.length() > 10) {
                    groupNameET.setError("Group name must be maximum of 10 characters only!");
                    groupNameET.requestFocus();
                } else if (TextUtils.isEmpty(groupMaxMembers)) {
                    groupMaxMembersET.setError("Please enter max members!");
                    groupMaxMembersET.requestFocus();
                }else if (Integer.valueOf(groupMaxMembers) > 10) {
                    groupMaxMembersET.setError("Max members must be less than or equal to 10!");
                    groupMaxMembersET.requestFocus();
                } else {
                    loadingBar.setTitle("Creating Group");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    int maxMembers = Integer.valueOf(groupMaxMembers);

                    final String groupToken = generateGroupToken();

                    Calendar callForDate = Calendar.getInstance();
                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
                    String date = currentDate.format(callForDate.getTime());

                    Calendar calForTime = Calendar.getInstance();
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
                    String time = currentTime.format(calForTime.getTime());

                    groupReference.child(groupToken).child("group_created_date").setValue(date);
                    groupReference.child(groupToken).child("group_created_time").setValue(time);
                    groupReference.child(groupToken).child("group_name").setValue(groupName);
                    groupReference.child(groupToken).child("max_members").setValue(maxMembers).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                groupReference.child(groupToken).child("members").child(currentUserId).child("name").setValue(currentUserName);
                                groupReference.child(groupToken).child("members").child(currentUserId).child("role").setValue("admin");
                                groupReference.child(groupToken).child("members").child(currentUserId).child("user_id").setValue(currentUserId)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    totalExpenditureReference.child(groupToken).child(currentUserId).child("name").setValue(currentUserName);
                                                    totalExpenditureReference.child(groupToken).child(currentUserId).child("total_amount").setValue(0);
                                                    totalExpenditureReference.child(groupToken).child(currentUserId).child("user_id").setValue(currentUserId)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {
                                                                        userReference.child("group_id").setValue(groupToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()) {
                                                                                    SharedPreferences.Editor editor = sp.edit();
                                                                                    editor.putString(Keys.ROLE, "admin");
                                                                                    editor.putBoolean(Keys.IS_LOGGED_IN, true);
                                                                                    editor.apply();

                                                                                    loadingBar.dismiss();

                                                                                    Intent intent = new Intent(CreateGroupActivity.this, AdminActivity.class);
                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    startActivity(intent);
                                                                                } else {
                                                                                    loadingBar.dismiss();
                                                                                    Snackbar.make(view, "Error occurred, please try again!", Snackbar.LENGTH_LONG).show();
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
                    });
                }
            }
        });
    }

    public String generateGroupToken() {

        String string = "0123456789";
        Random random = new Random();

        StringBuilder token = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            token.append(string.charAt(random.nextInt(string.length())));
        }

        return token.toString();
    }
}
