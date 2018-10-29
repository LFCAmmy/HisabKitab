package com.susankya.abhinav.myrealmapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.susankya.abhinav.myrealmapplication.General.Keys;
import com.susankya.abhinav.myrealmapplication.R;

public class RegisterActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView appName = findViewById(R.id.app_name_tv);
        final EditText usernameET = findViewById(R.id.username_et);
        final EditText emailET = findViewById(R.id.email_et);
        final EditText passwordET = findViewById(R.id.password_et);
        Button btnRegister = findViewById(R.id.register_btn);

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        appName.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        usernameET.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        emailET.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        passwordET.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        btnRegister.setAnimation(animation);

        final ProgressDialog progressDialog = new ProgressDialog(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                final String name = usernameET.getText().toString();
                final String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    usernameET.setError("Please enter your name!");
                    usernameET.requestFocus();
                }
                else if (TextUtils.isEmpty(email)) {
                    emailET.setError("Please enter your email!");
                    emailET.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailET.setError("Please enter a valid email address!");
                    emailET.requestFocus();
                }
                else if (TextUtils.isEmpty(password)) {
                    passwordET.setError("Please enter your password!");
                    passwordET.requestFocus();
                }else if (password.length() < 5) {
                    passwordET.setError("Password must be at least six characters long!");
                    passwordET.requestFocus();
                }else {
                    progressDialog.setTitle("Creating new account");
                    progressDialog.setMessage("This should only take a moment!");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
                                        userReference.child("user_name").setValue(name);
                                        userReference.child("group_id").setValue("none");
                                        userReference.child("user_email").setValue(email)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {
                                                            SharedPreferences sp = getSharedPreferences(Keys.PREFERENCES, 0);
                                                            SharedPreferences.Editor editor = sp.edit();
                                                            editor.putBoolean(Keys.IS_LOGGED_IN, true);
                                                            editor.apply();

                                                            progressDialog.dismiss();

                                                            Intent intent = new Intent(RegisterActivity.this, JoinGroupActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                        } else {
                                                            progressDialog.dismiss();
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
