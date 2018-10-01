package susankyatech.com.hisabkitab.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

    private SharedPreferences sp;

    private FirebaseAuth mAuth;
    private DatabaseReference userReference, groupReference;

    private String currentUserId, currentGroupId, userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView appName = findViewById(R.id.app_name);
        userEmailET = findViewById(R.id.email_et);
        userPasswordET = findViewById(R.id.password_et);
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

        loadingBar = new ProgressDialog(this);

        sp = getSharedPreferences("UserInfo", 0);

        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = userEmailET.getText().toString();
                String password = userPasswordET.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    userEmailET.setError("Please enter your email!");
                    userEmailET.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    userPasswordET.setError("Please enter your password!");
                    userPasswordET.requestFocus();
                } else {
                    loadingBar.setTitle("Logging In");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.show();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        currentUserId = mAuth.getCurrentUser().getUid();
                                        userReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.exists()) {
                                                    currentGroupId = dataSnapshot.child("group_id").getValue().toString();

                                                    if (currentGroupId.equals("none")){
                                                        SharedPreferences.Editor editor = sp.edit();
                                                        editor.putString("role", "none");
                                                        editor.apply();

                                                        Intent intent = new Intent(Login.this, Welcome.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                    }else {
                                                        groupReference.child(currentGroupId).child("members").child(currentUserId)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                        if (dataSnapshot.exists()) {
                                                                            userRole = dataSnapshot.child("role").getValue().toString();

                                                                            if (userRole.equals("admin")) {
                                                                                SharedPreferences.Editor editor = sp.edit();
                                                                                editor.putString("role", "admin");
                                                                                editor.apply();

                                                                                Intent intent = new Intent(Login.this, AdminMain.class);
                                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                startActivity(intent);
                                                                            } else {
                                                                                SharedPreferences.Editor editor = sp.edit();
                                                                                editor.putString("role", "member");
                                                                                editor.apply();

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
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(Login.this, "Email/Password not matched. Try again!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            userRole = sp.getString("role", "apple");

            Log.d("Intel", "" + userRole);

            switch (userRole) {
                case "admin": {
                    Intent intent = new Intent(Login.this, AdminMain.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                }
                case "member": {
                    Intent intent = new Intent(Login.this, MemberMain.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                }
                default: {
                    Intent intent = new Intent(Login.this, Welcome.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                }
            }
        }
    }
}