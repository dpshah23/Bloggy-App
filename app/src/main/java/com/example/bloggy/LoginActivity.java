package com.example.bloggy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    EditText EmailText;
    EditText PasswordText;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EmailText = findViewById(R.id.emailaddress);
        PasswordText = findViewById(R.id.password);
        login = findViewById(R.id.loginbtn);

        login.setOnClickListener(v -> {
            String email = EmailText.getText().toString();
            String password = PasswordText.getText().toString();

            // Validate input
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
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


                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Response: " + response.toString(), Toast.LENGTH_LONG).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "POST request failed. Response Code: " + responseCode, Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("LoginActivity", "Request failed", e); // Log detailed error
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
                }
            }).start();
        });
    }
}
