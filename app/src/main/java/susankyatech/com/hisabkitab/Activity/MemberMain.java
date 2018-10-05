package susankyatech.com.hisabkitab.Activity;

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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import susankyatech.com.hisabkitab.Fragments.AboutFragment;
import susankyatech.com.hisabkitab.Fragments.CurrentExpenses;
import susankyatech.com.hisabkitab.Fragments.GroupExpenses;
import susankyatech.com.hisabkitab.R;

public class MemberMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private CircleImageView navGroupImageDisplay;
    private TextView navUserEmailDisplayTV, navGroupNameDisplayTV;

    private SharedPreferences sp;

    private FirebaseAuth mAuth;
    private DatabaseReference userReference, groupReference;

    private String currentUserId, currentGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_activity_main);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportFragmentManager().beginTransaction().add(R.id.content_main_frame, new CurrentExpenses()).commit();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeader = navigationView.getHeaderView(0);
        navGroupImageDisplay = navHeader.findViewById(R.id.group_image_display);
        navGroupNameDisplayTV = navHeader.findViewById(R.id.group_name_tv);
        navUserEmailDisplayTV = navHeader.findViewById(R.id.user_email_tv);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        sp = getSharedPreferences("UserInfo", 0);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");

        init();
    }

    private void init() {
        
        userReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue().toString();

                    if (currentGroupId.equals("none")) {

                        SharedPreferences.Editor editor = sp.edit();
                        editor.remove("role");
                        editor.apply();

                        Intent intent = new Intent(MemberMain.this, Welcome.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                    String userEmail = dataSnapshot.child("user_email").getValue().toString();
                    navUserEmailDisplayTV.setText(userEmail);

                    groupReference.child(currentGroupId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                String groupImage = dataSnapshot.child("group_image").getValue().toString();
                                String groupName = dataSnapshot.child("group_name").getValue().toString();
                                Picasso.get().load(groupImage).into(navGroupImageDisplay);
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
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_frame, new AboutFragment()).commit();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.nav_current_expenses: {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_frame, new CurrentExpenses()).commit();
                break;
            }
            case R.id.nav_group_expenses: {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_main_frame, new GroupExpenses()).commit();
                break;
            }
            case R.id.nav_logout: {
                mAuth.signOut();

                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(MemberMain.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
