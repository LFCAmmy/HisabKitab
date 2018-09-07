package susankyatech.com.hisabkitab;

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

public class RegisterActivity extends AppCompatActivity {

    private TextView appName;
    private EditText newUsername, newEmail, newPassword;
    private Button btnRegister;

    private Toolbar toolbar;
    private Animation animation;

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDatabaseReference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        appName = findViewById(R.id.app_name);
        newUsername = findViewById(R.id.new_username_field);
        newEmail = findViewById(R.id.new_email_field);
        newPassword = findViewById(R.id.new_password_field);
        btnRegister = findViewById(R.id.register_btn);

        toolbar = findViewById(R.id.register_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        appName.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        newUsername.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        newEmail.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        newPassword.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_left);
        btnRegister.setAnimation(animation);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = newUsername.getText().toString();
                String email = newEmail.getText().toString();
                String password = newPassword.getText().toString();

                RegisterAccount(name, email, password);
            }
        });
    }

    private void RegisterAccount(final String name, String email, String password) {

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(RegisterActivity.this, "Name field is empty!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(RegisterActivity.this, "Email field is empty!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Password field is empty!", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String currentUserId = mAuth.getCurrentUser().getUid();
                                storeUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("user_list").child(currentUserId);
                                storeUserDatabaseReference.child("user_name").setValue(name)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(RegisterActivity.this, "Error occurred, please try again!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }
}
