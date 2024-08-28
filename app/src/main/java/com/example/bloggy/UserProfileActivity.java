package com.example.bloggy;

import android.annotation.SuppressLint;
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
        followdata=findViewById(R.id.follow);

        SharedPreferences hred = getSharedPreferences("demo", MODE_PRIVATE);
        String username_following = hred.getString("username", "");

        String username= getIntent().getStringExtra("username");

        String jsonPayload = String.format("{\"username\":\"%s\",\"username_following\":\"%s\"}", username,username_following);

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
                            Toast.makeText(UserProfileActivity.this, "User Doesn't Exist", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                String usernameDisp = obj.getString("username");
                                String phone = obj.getString("phone");
                                String email = obj.getString("email");
                                String name = obj.getString("name");
                                String profilePhotoUrl = obj.getString("avatar");
                                boolean isfollow=obj.getBoolean("is_following");
                                Log.d("ProfileActivity", "Profile Photo URL: " + profilePhotoUrl);

                                if(isfollow){
                                    followdata.setText("Following");
                                }
                                else{
                                    followdata.setText("Follow");
                                }


                                emaildata.setText(email);
                                usernamedata.setText(usernameDisp);
                                namedata.setText(name);


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
                System.out.println(e);
            }
        }).start();

    }
}