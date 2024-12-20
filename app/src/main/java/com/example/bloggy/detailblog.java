package com.example.bloggy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class detailblog extends AppCompatActivity {

    TextView disptitle;
    TextView dispcontent;
    TextView disptime;
    TextView dispuser;
    TextView dispviews;
    ImageView dispimage;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_detailblog);



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnav);
        bottomNavigationView.setSelectedItemId(R.id.home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String blogId = getIntent().getStringExtra("BLOG_ID");
        System.out.println(blogId);
        String jsonPayload=String.format("{\"blogid\":\"%s\"}",blogId);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    startActivity(new Intent(detailblog.this, HomeActivity.class));
                    return true;
                } else if (itemId == R.id.create) {
                    startActivity(new Intent(detailblog.this, CreateBlogActivity.class));
                    return true;
                } else if (itemId == R.id.myblog) {
                    startActivity(new Intent(detailblog.this, MyBlogsActivity.class));
                    return true;
                } else if (itemId == R.id.profile) {
                    startActivity(new Intent(detailblog.this, ProfileActivity.class));
                    return true;
                }

                return false;
            }
        });



        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            disptitle = findViewById(R.id.title);
            dispcontent = findViewById(R.id.content_text);
            disptime = findViewById(R.id.timestamp);
            dispuser = findViewById(R.id.username);
            dispviews = findViewById(R.id.views_count);
            dispimage = findViewById(R.id.image);

            try {
                String urlString = String.format("http://10.0.2.2:8000/api/getblog/%s/", blogId);
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
                            Toast.makeText(detailblog.this, "Blog is unavailable", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                String content = obj.getString("content").toString();
                                String username = obj.getString("username").toString();
                                String image = obj.getString("image").toString();
                                String timestamp = obj.getString("timestamp").toString();
                                String views = obj.getString("views").toString();
                                String title = obj.getString("title").toString();


                                String decodedData = URLDecoder.decode(title, "UTF-8");
                                String decodedDataDescription = URLDecoder.decode(content, "UTF-8");

                                dispcontent.setText(decodedDataDescription);
                                disptitle.setText(decodedData);
                                dispviews.setText(views);
                                disptime.setText(timestamp);
                                dispuser.setText(username);

                                dispuser.setOnClickListener(v -> {
                                    Intent intent = new Intent(detailblog.this, UserProfileActivity.class);
                                    intent.putExtra("username", username);
                                    startActivity(intent);
                                });


                                Glide.with(detailblog.this)
                                        .load(image)
                                        .into(dispimage);

                            } catch (Exception e) {
                                Log.e("detailblog", "Error parsing JSON", e);
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