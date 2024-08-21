package com.example.bloggy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForgetPassword extends AppCompatActivity {

    EditText email;
    Button forgetbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        email = findViewById(R.id.emailInput);
        forgetbtn = findViewById(R.id.forgetpasswordbtnsubmit);

        forgetbtn.setOnClickListener(v -> {
            String emailinp = email.getText().toString();

            System.out.println(emailinp);

            // Correct the JSON payload format
            String jsonPayload = String.format("{\"email\":\"%s\"}", emailinp);

            new Thread(() -> {
                HttpURLConnection connection = null;
                BufferedReader reader = null;

                try {
                    String urlString = "http://10.0.2.2:8000/api-auth/forgetpassword/";
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

                        JSONObject obj = new JSONObject(response.toString());
                        String message = obj.getString("message");

                        runOnUiThread(() -> {
                            if (message.equals("Mail sent")) {
                                Toast.makeText(ForgetPassword.this, "Mail Sent To Your Email", Toast.LENGTH_SHORT).show();

                                Intent intent=new Intent(ForgetPassword.this,LoginActivity.class);
                                startActivity(intent);
                                finish();


                            } else if (message.equals("Email not found")) {
                                Toast.makeText(ForgetPassword.this, "User Doesn't Exist.", Toast.LENGTH_SHORT).show();

                                Intent intent=new Intent(ForgetPassword.this,LoginActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(ForgetPassword.this, "Error Occurred", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(ForgetPassword.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ForgetPassword.this, "POST request failed. Response Code: " + responseCode, Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("ForgetPassword", "Request failed", e);
                    runOnUiThread(() -> {
                        Toast.makeText(ForgetPassword.this, "Request failed." + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                }
            }).start();
        });
    }
}
