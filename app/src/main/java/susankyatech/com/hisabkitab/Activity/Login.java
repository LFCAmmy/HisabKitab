package susankyatech.com.hisabkitab.Activity;

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

import susankyatech.com.hisabkitab.R;

public class Login extends AppCompatActivity {

    private EditText userEmailET, userPasswordET;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference userReference, memberReference;

    private String currentUserId, currentGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView appName = findViewById(R.id.app_name);
        userEmailET = findViewById(R.id.email_et);
        userPasswordET = findViewById(R.id.password_et);
        Button loginBtn = findViewById(R.id.login_btn);
        TextView createNewAccount = findViewById(R.id.create_new_account_tv);

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.from_top);
        appName.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_bottom);
        userEmailET.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_bottom);
        userPasswordET.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_bottom);
        loginBtn.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.from_bottom);
        createNewAccount.setAnimation(animation);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = userEmailET.getText().toString();
                String password = userPasswordET.getText().toString();

                logInUserAccount(email, password);
            }
        });

        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

    }

    private void logInUserAccount(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            userEmailET.setError("Please enter your email!");
            userEmailET.requestFocus();
        }
        else if (TextUtils.isEmpty(password)) {
            userPasswordET.setError("Please enter your password!");
            userPasswordET.requestFocus();
        } else {
            loadingBar.setTitle("Logging In");
            loadingBar.setMessage("Please wait!");
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                currentUserId = mAuth.getCurrentUser().getUid();
                                userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
                                userReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        currentGroupId = dataSnapshot.child("group_id").getValue().toString();

                                        if (!currentGroupId.equals("none")) {
                                            memberReference = FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroupId).child("members").child(currentUserId);
                                            memberReference.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()){
                                                        String status = dataSnapshot.child("role").getValue().toString();

                                                        if (status.equals("admin")) {
                                                            Intent intent = new Intent(Login.this, AdminMain.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                        }

                                                        if (status.equals("member")) {
                                                            Intent intent = new Intent(Login.this, MemberMain.class);
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
                                        else {
                                            Intent intent = new Intent(Login.this, Welcome.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                Toast.makeText(Login.this, "Email/Password not matched. Try again!", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(Login.this, AdminMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
