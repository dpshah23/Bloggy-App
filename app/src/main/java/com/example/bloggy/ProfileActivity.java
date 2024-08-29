package com.example.bloggy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {

    ImageView profile_avatar;

    TextView emaildata, usernamedata, phonenodata, namedata , followerdisp , followingdisp;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnav);
        bottomNavigationView.setSelectedItemId(R.id.profile);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the LogoutActivity
                Intent intent = new Intent(ProfileActivity.this, LogoutActivity.class);
                startActivity(intent);
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                    return true;
                } else if (itemId == R.id.create) {
                    startActivity(new Intent(ProfileActivity.this, CreateBlogActivity.class));
                    return true;
                } else if (itemId == R.id.myblog) {
                    startActivity(new Intent(ProfileActivity.this, MyBlogsActivity.class));
                    return true;
                } else if (itemId == R.id.profile) {
                    startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                    return true;
                }

                return false;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emaildata = findViewById(R.id.profile_email);
        usernamedata = findViewById(R.id.profile_username);
        phonenodata = findViewById(R.id.profile_phone);
        namedata = findViewById(R.id.profile_name);
        followerdisp=findViewById(R.id.profile_followers);
        followingdisp=findViewById(R.id.profile_following);

        profile_avatar = findViewById(R.id.profile_avatar);

        SharedPreferences hred = getSharedPreferences("demo", MODE_PRIVATE);
        String username = hred.getString("username", "");

        String jsonPayload = String.format("{\"username\":\"%s\"", username);

        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                String urlString = String.format("http://10.0.2.2:8000/api/profile/%s/", username);
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

                    System.out.println(response);
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    String message = obj.getString("message");

                    runOnUiThread(() -> {
                        if (message.equals("Failed")) {
                            Toast.makeText(ProfileActivity.this, "User Doesn't Exist", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                String usernameDisp = obj.getString("username");
                                String phone = obj.getString("phone");
                                String email = obj.getString("email");
                                String name = obj.getString("name");
                                String profilePhotoUrl = obj.getString("avatar");
                                Integer totalfollowers=obj.getInt("total");
                                Integer totalfollowing=obj.getInt("following_total");

                                Log.d("ProfileActivity", "Profile Photo URL: " + profilePhotoUrl);


                                emaildata.setText(email);
                                usernamedata.setText(usernameDisp);
                                phonenodata.setText(phone);
                                namedata.setText(name);

                                followerdisp.setText(String.valueOf(totalfollowers));
                                followingdisp.setText(String.valueOf(totalfollowing));


                                Glide.with(ProfileActivity.this)
                                        .load(profilePhotoUrl)
                                        .into(profile_avatar);

                            } catch (Exception e) {
                                Log.e("ProfileActivity", "Error parsing JSON", e);
                            }
                        }
                    });


                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }).start();

    }
}