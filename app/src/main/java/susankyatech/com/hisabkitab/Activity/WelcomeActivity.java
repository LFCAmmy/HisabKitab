package susankyatech.com.hisabkitab.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import susankyatech.com.hisabkitab.Fragment.GettingStartedFragment;
import susankyatech.com.hisabkitab.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getSupportFragmentManager().beginTransaction().add(R.id.content_welcome_frame,new GettingStartedFragment()).commit();
        
    }
}
