package com.example.bloggy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Struct;

public class LoginActivity extends AppCompatActivity {

    EditText EmailText;
    EditText PasswordText;
    Button login;
    TextView signup;
    TextView forget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);


        setContentView(R.layout.activity_login);

        EmailText = findViewById(R.id.emailaddress);
        PasswordText = findViewById(R.id.password);
        login = findViewById(R.id.loginbtn);

        forget=findViewById(R.id.forgetpass);

        signup=findViewById(R.id.createaccount);

        signup.setOnClickListener(v -> {
            Intent intent=new Intent(LoginActivity.this, SignUp.class);
            startActivity(intent);

        });

        forget.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgetPassword.class);
            startActivity(intent);

        });

        login.setOnClickListener(v -> {
            String email = EmailText.getText().toString();
            String password = PasswordText.getText().toString();

            // Validate input
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Email And Password Both Are Mandatory fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create JSON payload
            String jsonPayload = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);

            new Thread(() -> {
                HttpURLConnection connection = null;
                BufferedReader reader = null;

                try {
                    String urlString = "http://10.0.2.2:8000/api-auth/login/";
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);


                    OutputStream os = connection.getOutputStream();
                    os.write(jsonPayload.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    // Read response
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject obj=new JSONObject(String.valueOf(response));
                        String message=obj.getString("message");

                        if(message.equals("Login failed")){
                            runOnUiThread(()->{
                                Toast.makeText(LoginActivity.this,"Invalid Email or Pasword",Toast.LENGTH_SHORT).show();
                            });
                        }
                        else{

                            SharedPreferences shred=getSharedPreferences("demo",MODE_PRIVATE);
                            SharedPreferences.Editor editor=shred.edit();
                            String email12 = EmailText.getText().toString();

                            String username=obj.getString("username");

                            editor.putString("login","done");
                            editor.putString("email",email12);
                            editor.putString("username",username);
                            editor.apply();



                            Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "POST request failed. Response Code: " + responseCode, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            catch (Exception e) {
                    e.printStackTrace();
                    Log.e("LoginActivity", "Request failed", e);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Request failed."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }}).start();
        });
    }
}
