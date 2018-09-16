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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText userEmail, userPassword;
    private Button loginBtn;
    private TextView appName, createNewAccount;

    private ProgressDialog loadingBar;
    private Animation animation;

    private String currentUserId, currentGroupId;

    private FirebaseAuth mAuth;
    private DatabaseReference userReference, groupReference;

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
            userEmail.setError("Please enter your email");
            userEmail.requestFocus();
        }
        else if (TextUtils.isEmpty(password)) {
            userPassword.setError("Please enter your password");
            userPassword.requestFocus();
        } else {
            loadingBar.setTitle("Logging In");
            loadingBar.setMessage("Please wait!");
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

//                                currentUserId = mAuth.getCurrentUser().getUid();
//                                userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
//                                userReference.addValueEventListener(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                                        if (dataSnapshot.exists()) {
//                                            currentGroupId = dataSnapshot.child("group_id").getValue().toString();
//
//                                            groupReference = FirebaseDatabase.getInstance().getReference().child(currentGroupId).child("members").child(currentUserId);
//                                            groupReference.addValueEventListener(new ValueEventListener() {
//                                                @Override
//                                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                                    String userStatus = dataSnapshot.child("role").getValue().toString();
//
//                                                    if (userStatus.equals("admin")) {
//
//                                                    }
//
//                                                }
//
//                                                @Override
//                                                public void onCancelled(DatabaseError databaseError) {
//
//                                                }
//                                            });
//
//                                        }
//
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//
//                                    }
//                                });

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

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sendUserToMainActivity();
        }
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
