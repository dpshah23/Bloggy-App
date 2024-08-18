package com.example.bloggy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


public class HomeActivity extends AppCompatActivity {

    Button createblog1;

    Button logoutbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        logoutbutton=findViewById(R.id.logoutbtn);

        createblog1=findViewById(R.id.createblog);

        createblog1.setOnClickListener(v -> {
            Intent intent=new Intent(HomeActivity.this,CreateBlogActivity.class);
            startActivity(intent);
        });
        logoutbutton.setOnClickListener(v -> {
            Intent intent=new Intent(HomeActivity.this,LogoutActivity.class);
            startActivity(intent);
            finish();
        });
    }

}