package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SectionsPagerAdapter sectionsPagerAdapter;

    @BindView(R.id.main_page_toolbar)
    Toolbar mToolBar;
    @BindView(R.id.mainPager)
    ViewPager viewPager;
    @BindView(R.id.main_tabs)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Chatter");

        mAuth = FirebaseAuth.getInstance();
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            sendToStart();
        }
    }

    private void sendToStart() {
            startActivity(new Intent(getApplicationContext(), StartActivity.class));
            finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.btn_acc_settings){
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }
        else if (item.getItemId() == R.id.btn_all_user){
            startActivity(new Intent(getApplicationContext(), AllUserActivity.class));
        }
        else if (item.getItemId() == R.id.btn_main_logout){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

//        switch (item.getItemId()){
//            case R.id.btn_acc_settings:

//            case R.id.btn_all_user:
//            case R.id.btn_main_logout:

//        }
        return true;
    }
}
