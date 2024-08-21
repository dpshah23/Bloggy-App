package com.example.bloggy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
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
    EditText username;
    EditText phone;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        name = findViewById(R.id.nameInput);
        username = findViewById(R.id.usernameInput);
        phone = findViewById(R.id.phoneInput);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        button = findViewById(R.id.signupbtn);

        button.setOnClickListener(v -> {
            String nameinput = name.getText().toString();
            String usernameinput = username.getText().toString();
            String phoneinput = phone.getText().toString();
            String emailinput = email.getText().toString();
            String passwordinput = password.getText().toString();

            if (emailinput.isEmpty() || nameinput.isEmpty() || passwordinput.isEmpty() || usernameinput.isEmpty() || phoneinput.isEmpty()) {
                Toast.makeText(SignUp.this, "All Details Are Mandatory", Toast.LENGTH_SHORT).show();
                return;
            } else {
                String jsonPayload = String.format("{\"email\":\"%s\", \"password\":\"%s\", \"name\":\"%s\", \"username\":\"%s\", \"phone\":\"%s\"}",
                        emailinput, passwordinput, nameinput, usernameinput, phoneinput);

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

                            JSONObject successMessage = new JSONObject(response.toString());
                            String message = successMessage.getString("message");

                            if (message.equals("username_exists")) {
                                runOnUiThread(() -> {
                                    Toast.makeText(SignUp.this, "Username Already Exists", Toast.LENGTH_SHORT).show();
                                });
                            } else if (message.equals("created")) {
                                runOnUiThread(() -> {
                                    Toast.makeText(SignUp.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignUp.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(SignUp.this, "Account Creation Failed", Toast.LENGTH_SHORT).show();
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
