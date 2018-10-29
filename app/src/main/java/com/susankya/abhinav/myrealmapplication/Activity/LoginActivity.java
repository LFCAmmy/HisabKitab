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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.susankya.abhinav.myrealmapplication.General.Keys;
import com.susankya.abhinav.myrealmapplication.R;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sp = getSharedPreferences(Keys.PREFERENCES, 0);

        boolean isUserLoggedIn = sp.getBoolean(Keys.IS_LOGGED_IN, false);

        if (isUserLoggedIn) {
            checkUserRole();
        } else {
            TextView appName = findViewById(R.id.app_name_tv);
            final EditText userEmailET = findViewById(R.id.email_et);
            final EditText userPasswordET = findViewById(R.id.password_et);
            Button loginBtn = findViewById(R.id.login_btn);
            TextView createNewAccount = findViewById(R.id.create_new_account_tv);

            Animation animation = AnimationUtils.loadAnimation(this, R.anim.from_top);
            appName.setAnimation(animation);

            animation = AnimationUtils.loadAnimation(this, R.anim.from_bottom);
            userEmailET.setAnimation(animation);

            animation = AnimationUtils.loadAnimation(this, R.anim.from_bottom);
            userPasswordET.setAnimation(animation);

            animation = AnimationUtils.loadAnimation(this, R.anim.from_bottom);
            loginBtn.setAnimation(animation);

            animation = AnimationUtils.loadAnimation(this, R.anim.from_bottom);
            createNewAccount.setAnimation(animation);

            final ProgressDialog progressDialog = new ProgressDialog(this);

            final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users");
            final DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference().child("Group");

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {

                    String email = userEmailET.getText().toString();
                    String password = userPasswordET.getText().toString();

                    if (TextUtils.isEmpty(email)) {
                        userEmailET.setError("Please enter your email!");
                        userEmailET.requestFocus();
                    } else if (TextUtils.isEmpty(password)) {
                        userPasswordET.setError("Please enter your password!");
                        userPasswordET.requestFocus();
                    } else {
                        progressDialog.setTitle("Logging In");
                        progressDialog.setMessage("This should only take a moment!");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (task.isSuccessful()) {
                                            final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                            userReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    if (dataSnapshot.exists()) {
                                                        final String currentGroupId = dataSnapshot.child("group_id").getValue(String.class);

                                                        if (currentGroupId.equals("none")) {

                                                            SharedPreferences.Editor editor = sp.edit();
                                                            editor.putString(Keys.ROLE, "none");
                                                            editor.putBoolean(Keys.IS_LOGGED_IN, true);
                                                            editor.apply();

                                                            Intent intent = new Intent(LoginActivity.this, JoinGroupActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                        } else {
                                                            groupReference.child(currentGroupId).child("members").child(currentUserId)
                                                                    .addValueEventListener(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                            if (dataSnapshot.exists()) {
                                                                                String userRole = dataSnapshot.child("role").getValue(String.class);

                                                                                if (userRole.equals("admin")) {
                                                                                    SharedPreferences.Editor editor = sp.edit();
                                                                                    editor.putString(Keys.ROLE, "admin");
                                                                                    editor.putBoolean(Keys.IS_LOGGED_IN, true);
                                                                                    editor.apply();

                                                                                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    startActivity(intent);
                                                                                } else {
                                                                                    SharedPreferences.Editor editor = sp.edit();
                                                                                    editor.putString(Keys.ROLE, "member");
                                                                                    editor.putBoolean(Keys.IS_LOGGED_IN, true);
                                                                                    editor.apply();

                                                                                    Intent intent = new Intent(LoginActivity.this, MemberActivity.class);
                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    startActivity(intent);
                                                                                }
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        } else {
                                            progressDialog.dismiss();
                                            Snackbar.make(view, "Invalid email or password, try again!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                }
            });

            createNewAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                }
            });
        }
    }

    private void checkUserRole() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {

            String userRole = sp.getString(Keys.ROLE, "none");

            switch (userRole) {
                case "admin": {
                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                }
                case "member": {
                    Intent intent = new Intent(LoginActivity.this, MemberActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                }
                default: {
                    Intent intent = new Intent(LoginActivity.this, JoinGroupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                }
            }
        }

    }
}
