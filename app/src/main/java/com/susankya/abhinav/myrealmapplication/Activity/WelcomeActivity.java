package com.susankya.abhinav.myrealmapplication.activity;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.susankya.abhinav.myrealmapplication.fragments.QRCode;
import com.susankya.abhinav.myrealmapplication.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_activitiy);

        String condition = getIntent().getStringExtra("fragment");

        switch (condition) {

            case "qr_code": {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_main_frame, new QRCode());
                transaction.commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

       startActivity(new Intent(WelcomeActivity.this, JoinGroupActivity.class));
       finish();
    }
}
