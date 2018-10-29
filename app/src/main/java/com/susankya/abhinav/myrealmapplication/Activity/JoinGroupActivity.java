package com.susankya.abhinav.myrealmapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class JoinGroupActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 1;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        Button logOutBtn = findViewById(R.id.logout_btn);
        final EditText groupCodeET = findViewById(R.id.group_code_et);
        Button joinGroupBtn = findViewById(R.id.join_group_btn);
        ImageButton qrCodeBtn = findViewById(R.id.qr_code_btn);
        TextView createNewGroupTV = findViewById(R.id.create_group_tv);

        final ProgressDialog loadingBar = new ProgressDialog(this);

        final SharedPreferences sp = getSharedPreferences(Keys.PREFERENCES, 0);

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        final DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference().child("Group");
        final DatabaseReference totalExpendituresReference = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    userName = dataSnapshot.child("user_name").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();

                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(JoinGroupActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        joinGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                final String code = groupCodeET.getText().toString();

                if (TextUtils.isEmpty(code)) {
                    groupCodeET.setError("Please enter group code to join!");
                    groupCodeET.requestFocus();
                } else {
                    loadingBar.setTitle("Joining Group");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    groupReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {

                                if (dataSnapshot.hasChild(code)) {
                                    long groupMembersCount = dataSnapshot.child(code).child("members").getChildrenCount();
                                    long maxMembersAllowed = Integer.valueOf(dataSnapshot.child(code).child("max_members").getValue().toString());

                                    if (maxMembersAllowed > groupMembersCount) {

                                        groupReference.child(code).child("members").child(currentUserId).child("name").setValue(userName);
                                        groupReference.child(code).child("members").child(currentUserId).child("role").setValue("member");
                                        groupReference.child(code).child("members").child(currentUserId).child("user_id").setValue(currentUserId)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {
                                                            totalExpendituresReference.child(code).child(currentUserId).child("name").setValue(userName);
                                                            totalExpendituresReference.child(code).child(currentUserId).child("total_amount").setValue(0);
                                                            totalExpendituresReference.child(code).child(currentUserId).child("user_id").setValue(currentUserId)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()) {
                                                                                userReference.child("group_id").setValue(code)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                if (task.isSuccessful()) {

                                                                                                    SharedPreferences.Editor editor = sp.edit();
                                                                                                    editor.putString(Keys.ROLE, "member");
                                                                                                    editor.putString(Keys.GROUP_ID, code);
                                                                                                    editor.putBoolean(Keys.IS_LOGGED_IN, true);
                                                                                                    editor.apply();

                                                                                                    loadingBar.dismiss();

                                                                                                    Intent intent = new Intent(JoinGroupActivity.this, MemberActivity.class);
                                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                                    startActivity(intent);
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    } else {
                                        loadingBar.dismiss();
                                        Snackbar.make(view, "Sorry, this group has already its maximum members joined in the group!", Snackbar.LENGTH_LONG)
                                                .show();
                                    }
                                } else {
                                    loadingBar.dismiss();
                                    Snackbar.make(view, "No group found. Please enter a valid group code to join!", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        qrCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(JoinGroupActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(JoinGroupActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                } else {
                    Intent intent = new Intent(JoinGroupActivity.this, WelcomeActivity.class);
                    intent.putExtra("fragment", "qr_code");
                    startActivity(intent);
                    finish();
                }
            }
        });

        createNewGroupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(JoinGroupActivity.this, CreateGroupActivity.class));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case CAMERA_PERMISSION_CODE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(JoinGroupActivity.this, WelcomeActivity.class);
                    intent.putExtra("fragment", "qr_code");
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Camera permission has been denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
