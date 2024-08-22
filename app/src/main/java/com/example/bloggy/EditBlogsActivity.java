package com.example.bloggy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditBlogsActivity extends AppCompatActivity {

    EditText title;
    EditText content;
    ImageView Blogimg;
    Button Update;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_edit_blogs);

        // Handle WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED; // Mark the insets as consumed
        });

        // Optional: You can also make the app edge-to-edge by hiding system bars
        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(findViewById(R.id.main));
        if (windowInsetsController != null) {
            windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        }

        Intent intent=getIntent();
        Bundle extras=intent.getBundleExtra("bundle");

        String blogid1=extras.getString("BlogId");

        String jsonPayload=String.format("{\"blogid\":\"%s\"}",blogid1);

        title=findViewById(R.id.blogTitle);
        content=findViewById(R.id.blogContent);
        Blogimg=findViewById(R.id.blogImage);
        Update=findViewById(R.id.selectImageButton);

        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                String urlString = String.format("http://10.0.2.2:8000/api/getblog/%s/",blogid1);
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
                            Toast.makeText(EditBlogsActivity.this, "Error Occureed", Toast.LENGTH_SHORT).show();
                        } else {
                            try {

                                String title1=obj.getString("title");
                                String content1=obj.getString("content");
                                String image=obj.getString("image");

                                Log.d("Edit Blog Activity", "blog photo Photo URL: " + image);



                                Glide.with(EditBlogsActivity.this)
                                        .load(image)
                                        .into(Blogimg);

                            } catch (Exception e) {
                                Log.e("Edit Blog Activity", "Error parsing JSON", e);
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
