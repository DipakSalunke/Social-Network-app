package com.example.dipak.socialnetwork;

import android.content.Intent;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity
{
        private NavigationView navigationView;
        private DrawerLayout drawerLayout;
        private ActionBarDrawerToggle actionBarDrawerToggle;
        private RecyclerView postList;
        private Toolbar mToolbar;

     private CircleImageView NavProfileImage;
     private TextView NavProfileUserName;

        private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

       String currentUserID;


       @Override
    protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);

           setContentView(R.layout.activity_main);

           mAuth= FirebaseAuth.getInstance();




         currentUserID = mAuth.getCurrentUser().getUid();





      UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


           mToolbar = (Toolbar) findViewById(R.id.main_app_toolbar);
           setSupportActionBar(mToolbar);
           getSupportActionBar().setTitle("Home");

           drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
            actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
           navigationView = (NavigationView) findViewById(R.id.navigation_view);


         View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

      NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
      NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);


           navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
               @Override
               public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                 UserMenuSelector(item);
                   return false;

               }
           });


       }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }else
        {
            CheckUserExistance();
        }
    }

    private void CheckUserExistance()
    {
           final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)) {
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();


    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

           if(actionBarDrawerToggle.onOptionsItemSelected(item))
           {
               return true;
           }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_Logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;


        }
    }

}

