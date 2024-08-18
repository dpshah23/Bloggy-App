package com.example.bloggy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogoutActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        SharedPreferences getshared=getSharedPreferences("demo",MODE_PRIVATE);
        String value=getshared.getString("login","none");

        String email=getshared.getString("email","none");

        if(email.equals("none")){

            SharedPreferences shred = getSharedPreferences("demo", MODE_PRIVATE);
            SharedPreferences.Editor editor = shred.edit();

            editor.remove("login");
            editor.remove("email");
            editor.apply();

            Intent intent=new Intent(LogoutActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            String jsonPayload = String.format("{\"email\":\"%s\"}", email);

            new Thread(() -> {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    String urlString = "http://10.0.2.2:8000/api-auth/logout/";
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

                        Intent intent=new Intent(LogoutActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(LogoutActivity.this, "Error code: " + responseCode, Toast.LENGTH_SHORT).show();
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("SignUpActivity", "Request failed", e);
                    runOnUiThread(() -> {
                        Toast.makeText(LogoutActivity.this, "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }
}