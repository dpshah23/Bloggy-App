package com.example.bloggy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserProfileActivity extends AppCompatActivity {

    TextView emaildata;
    TextView usernamedata;
    TextView namedata;
    ImageView profile_avatar;
    TextView followerdisp;
    TextView followingdisp;
    Button followdata;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emaildata = findViewById(R.id.profile_email);
        usernamedata = findViewById(R.id.profile_username);
        namedata = findViewById(R.id.profile_name);
        profile_avatar = findViewById(R.id.profile_avatar);
        followdata = findViewById(R.id.follow);
        followerdisp=findViewById(R.id.profile_followers);
        followingdisp=findViewById(R.id.profile_following);

        SharedPreferences hred = getSharedPreferences("demo", MODE_PRIVATE);
        String username_following = hred.getString("username", "");
        String username = getIntent().getStringExtra("username");

        String jsonPayload = String.format("{\"username\":\"%s\",\"username_following\":\"%s\"}", username, username_following);
        System.out.println(jsonPayload);

        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                String urlString = String.format("http://10.0.2.2:8000/api/getuserprofile/%s/", username);
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

                    JSONObject obj = new JSONObject(response.toString());
                    String message = obj.getString("message");

                    runOnUiThread(() -> {
                        if (message.equals("Failed")) {
                            Toast.makeText(UserProfileActivity.this, "User Doesn't Exist", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                String usernameDisp = obj.getString("username");
                                String email = obj.getString("email");
                                String name = obj.getString("name");
                                String profilePhotoUrl = obj.getString("avatar");
                                boolean isFollow = obj.getBoolean("is_following");
                                Integer followerdata=obj.getInt("total");
                                Integer followingdata=obj.getInt("following_total");

                                if(isFollow){
                                    followdata.setText("Following");
                                }
                                else{
                                    followdata.setText("Follow");
                                }

                                followerdisp.setText(String.valueOf(followerdata));
                                followingdisp.setText(String.valueOf(followingdata));

                                emaildata.setText(email);
                                usernamedata.setText(usernameDisp);
                                namedata.setText(name);

                                followdata.setOnClickListener(v -> new Thread(() -> {
                                    try {
                                        String followUrlString;
                                        if (followdata.getText().toString().equals("Following")) {
                                            followUrlString = String.format("http://10.0.2.2:8000/api/unfollowuser/%s/", usernameDisp);
                                        } else {
                                            followUrlString = String.format("http://10.0.2.2:8000/api/followuser/%s/", usernameDisp);
                                        }
                                        URL followUrl = new URL(followUrlString);
                                        HttpURLConnection followConnection = (HttpURLConnection) followUrl.openConnection();
                                        followConnection.setRequestMethod("POST");
                                        followConnection.setRequestProperty("Content-Type", "application/json");
                                        followConnection.setDoOutput(true);

                                        String followJsonPayload = String.format("{\"username\":\"%s\",\"follower\":\"%s\"}", usernameDisp, username_following);
                                        OutputStream followOs = followConnection.getOutputStream();
                                        followOs.write(followJsonPayload.getBytes("UTF-8"));
                                        followOs.flush();
                                        followOs.close();

                                        int followResponseCode = followConnection.getResponseCode();
                                        if (followResponseCode == HttpURLConnection.HTTP_OK) {
                                            BufferedReader followReader = new BufferedReader(new InputStreamReader(followConnection.getInputStream()));
                                            StringBuilder followResponse = new StringBuilder();
                                            String followLine;

                                            while ((followLine = followReader.readLine()) != null) {
                                                followResponse.append(followLine);
                                            }

                                            JSONObject followObj = new JSONObject(followResponse.toString());
                                            String followMessage = followObj.getString("message");

                                            runOnUiThread(() -> {
                                                if (followMessage.equals("Failed")) {
                                                    Toast.makeText(UserProfileActivity.this, "Action failed", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(UserProfileActivity.this, followMessage, Toast.LENGTH_SHORT).show();
                                                    if (followdata.getText().toString().equals("Following")) {
                                                        followdata.setText("Follow");
                                                    } else {
                                                        followdata.setText("Following");
                                                    }
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        Log.e("UserProfileActivity", "Error during follow/unfollow", e);
                                    }
                                }).start());

                                Glide.with(UserProfileActivity.this)
                                        .load(profilePhotoUrl)
                                        .into(profile_avatar);

                            } catch (Exception e) {
                                Log.e("ProfileActivity", "Error parsing JSON", e);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("UserProfileActivity", "Error fetching user profile", e);
            }
        }).start();
    }
}
