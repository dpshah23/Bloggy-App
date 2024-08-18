package com.example.bloggy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUp extends AppCompatActivity {

    EditText email;
    EditText password;
    EditText name;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        name = findViewById(R.id.nameInput);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        button = findViewById(R.id.signupbtn);

        button.setOnClickListener(v -> {
            String nameinput = name.getText().toString();
            String emailinput = email.getText().toString();
            String passwordinput = password.getText().toString();

            if (emailinput.isEmpty() || nameinput.isEmpty() || passwordinput.isEmpty()) {
                Toast.makeText(SignUp.this, "All Details Are Mandatory", Toast.LENGTH_SHORT).show();
                return;
            } else {
                // Corrected the String formatting for the JSON payload
                String jsonPayload = String.format("{\"email\":\"%s\", \"password\":\"%s\",\"name\":\"%s\"}", emailinput, passwordinput, nameinput);

                new Thread(() -> {
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;
                    try {
                        String urlString = "http://10.0.2.2:8000/api-auth/signup/";
                        URL url = new URL(urlString);

                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setDoOutput(true);

                        OutputStream os = connection.getOutputStream();
                        os.write(jsonPayload.getBytes("UTF-8"));
                        os.flush();
                        os.close();

                        int responseCode = connection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                            StringBuilder response = new StringBuilder();
                            String line;

                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }

                            JSONObject successmessage = new JSONObject(response.toString());

                            String message=successmessage.getString("message");

                            if(message.equals("exists"))
                            {
                                runOnUiThread(() -> {
                                    Toast.makeText(SignUp.this,"Email Already Exists",Toast.LENGTH_SHORT).show();
                                });

                            }
                            else{
                                runOnUiThread(()-> {
                                    Toast.makeText(SignUp.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignUp.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                            }

                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(SignUp.this, "Sign up failed. Error code: " + responseCode, Toast.LENGTH_SHORT).show();
                            });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("SignUpActivity", "Request failed", e);
                        runOnUiThread(() -> {
                            Toast.makeText(SignUp.this, "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            }
        });
    }
}
