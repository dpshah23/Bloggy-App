package com.example.bloggy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


public class HomeActivity extends AppCompatActivity {

    Button logoutbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        logoutbutton=findViewById(R.id.logoutbtn);

        logoutbutton.setOnClickListener(v -> {
            Intent intent=new Intent(HomeActivity.this,LogoutActivity.class);
            startActivity(intent);
            finish();
        });
    }

}