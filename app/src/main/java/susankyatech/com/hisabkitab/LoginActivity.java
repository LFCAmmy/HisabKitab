package susankyatech.com.hisabkitab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class LoginActivity extends AppCompatActivity {

    private EditText userEmail, userPassword;
    private Button loginBtn;
    private TextView appName, createNewAccount;

    private ProgressDialog loadingBar;
    private Animation animation;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        appName = findViewById(R.id.app_name);
        userEmail = findViewById(R.id.email_field);
        userPassword = findViewById(R.id.password_field);
        loginBtn = findViewById(R.id.login_btn);
        createNewAccount = findViewById(R.id.create_new_account);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_top);
        appName.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_bottom);
        userEmail.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_bottom);
        userPassword.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_bottom);
        loginBtn.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_bottom);
        createNewAccount.setAnimation(animation);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();

                logInUserAccount(email, password);
            }
        });

        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

    }

    private void logInUserAccount(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Email field is empty!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Password field is empty!", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Logging In");
            loadingBar.setMessage("Please wait!");
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(LoginActivity.this, "Email/Password not matched. Try again!", Toast.LENGTH_SHORT).show();
                            }
                            loadingBar.dismiss();
                        }
                    });
        }
    }
}
