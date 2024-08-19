package com.example.bloggy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import  android.widget.Button;

public class ForgetPassword extends AppCompatActivity {


    EditText email;
    Button forgetbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        email=findViewById(R.id.emailInput);
        forgetbtn=findViewById(R.id.forgetpasswordbtnsubmit);

        forgetbtn.setOnClickListener(v -> {
            String emailinp=email.getText().toString();

            System.out.println(emailinp);



        });
    }
}