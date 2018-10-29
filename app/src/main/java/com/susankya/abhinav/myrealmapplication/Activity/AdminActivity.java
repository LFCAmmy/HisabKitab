package com.susankya.abhinav.myrealmapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.susankya.abhinav.myrealmapplication.fragments.AboutApp;
import com.susankya.abhinav.myrealmapplication.fragments.CurrentExpenses;
import com.susankya.abhinav.myrealmapplication.fragments.GroupExpenses;
import com.susankya.abhinav.myrealmapplication.fragments.AdminQRCode;
import com.susankya.abhinav.myrealmapplication.fragments.ManageGroup;
import com.susankya.abhinav.myrealmapplication.R;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String currentGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_main);

        getSupportFragmentManager().beginTransaction().add(R.id.content_main_frame, new CurrentExpenses()).commit();

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        View navHeader = navigationView.getHeaderView(0);
        final TextView navUserEmailDisplayTV = navHeader.findViewById(R.id.user_email_tv);
        final TextView navGroupNameDisplayTV = navHeader.findViewById(R.id.group_name_tv);
        final TextView navGroupCodeDisplayTV = navHeader.findViewById(R.id.group_code_tv);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        final DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference().child("Group");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue(String.class);
                    navGroupCodeDisplayTV.setText(currentGroupId);

                    String userEmail = dataSnapshot.child("user_email").getValue(String.class);
                    navUserEmailDisplayTV.setText(userEmail);

                    groupReference.child(currentGroupId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                String groupName = dataSnapshot.child("group_name").getValue(String.class);
                                navGroupNameDisplayTV.setText(groupName);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int id = item.getItemId();

        switch (id) {
            case R.id.about: {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_frame, new AboutApp()).addToBackStack(null).commit();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_current_expenses: {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_frame, new CurrentExpenses()).addToBackStack(null).commit();
                break;
            }
            case R.id.nav_group_expenses: {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_frame, new GroupExpenses()).addToBackStack(null).commit();
                break;
            }
            case R.id.nav_manage_group: {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_frame, new ManageGroup()).addToBackStack(null).commit();
                break;
            }
            case R.id.nav_qr_code: {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_frame, new AdminQRCode()).addToBackStack(null).commit();
                break;
            }
            case R.id.nav_logout: {
                FirebaseAuth.getInstance().signOut();

                SharedPreferences sp = getSharedPreferences("UserInfo", 0);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
