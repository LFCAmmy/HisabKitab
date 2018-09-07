package susankyatech.com.hisabkitab.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import susankyatech.com.hisabkitab.Fragment.LoginFragment;
import susankyatech.com.hisabkitab.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportFragmentManager().beginTransaction().add(R.id.contain_login_frame,new LoginFragment()).commit();

    }
}
