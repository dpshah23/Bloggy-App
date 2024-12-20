package com.example.bloggy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        //get the value of sharedprefrence



        SharedPreferences getshared=getSharedPreferences("demo",MODE_PRIVATE);
        String value=getshared.getString("login","none");
        if(value.equals("none")){
            Intent intent=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Intent intent =new Intent(MainActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        }

    }
}