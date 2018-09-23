package susankyatech.com.hisabkitab.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import susankyatech.com.hisabkitab.R;

public class Register extends AppCompatActivity {

    private EditText newUsernameET, newEmailET, newPasswordET;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference userReference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar = new ProgressDialog(this);

        TextView appName = findViewById(R.id.app_name);
        newUsernameET = findViewById(R.id.new_username_et);
        newEmailET = findViewById(R.id.new_email_et);
        newPasswordET = findViewById(R.id.new_password_et);
        Button btnRegister = findViewById(R.id.register_btn);

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        appName.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        newUsernameET.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        newEmailET.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        newPasswordET.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        btnRegister.setAnimation(animation);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = newUsernameET.getText().toString();
                String email = newEmailET.getText().toString();
                String password = newPasswordET.getText().toString();

                RegisterAccount(name, email, password);
            }
        });
    }

    private void RegisterAccount(final String name, String email, String password) {

        if (TextUtils.isEmpty(name)) {
            newUsernameET.setError("Please enter your name!");
            newUsernameET.requestFocus();
        }
        else if (TextUtils.isEmpty(email)) {
            newEmailET.setError("Please enter your email!");
            newEmailET.requestFocus();
        }
        else if (TextUtils.isEmpty(password)) {
            newPasswordET.setError("Please enter your password!");
            newPasswordET.requestFocus();
        } else {
            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait while we are creating an account for you!");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                String currentUserId = mAuth.getCurrentUser().getUid();
                                userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
                                userReference.child("user_name").setValue(name);
                                userReference.child("group_id").setValue("none")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    Intent intent = new Intent(Register.this, Welcome.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(Register.this, "Error occurred, please try again!", Toast.LENGTH_SHORT).show();
                                                }
                                                loadingBar.dismiss();
                                            }
                                        });
                            }
                        }
                    });
        }
    }
}
