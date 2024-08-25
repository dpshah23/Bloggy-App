package com.example.bloggy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the LogoutActivity
                Intent intent = new Intent(HomeActivity.this, LogoutActivity.class);
                startActivity(intent);
            }
        });

    BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnav);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.create) {
                startActivity(new Intent(HomeActivity.this, CreateBlogActivity.class));
                return true;
            } else if (itemId == R.id.myblog) {
                startActivity(new Intent(HomeActivity.this, MyBlogsActivity.class));
                return true;
            } else if (itemId == R.id.profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }

            return false;
        }
    });
        }
}