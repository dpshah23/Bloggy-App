package com.example.bloggy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CreateBlogActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView blogImage;
    private EditText blogTitle, blogContent;
    private Button selectImageButton, submitBlogButton;
    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_blog);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnav);
        bottomNavigationView.setSelectedItemId(R.id.create);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    startActivity(new Intent(CreateBlogActivity.this, HomeActivity.class));
                    return true;
                } else if (itemId == R.id.create) {
                    startActivity(new Intent(CreateBlogActivity.this, CreateBlogActivity.class));
                    return true;
                } else if (itemId == R.id.myblog) {
                    startActivity(new Intent(CreateBlogActivity.this, MyBlogsActivity.class));
                    return true;
                } else if (itemId == R.id.profile) {
                    startActivity(new Intent(CreateBlogActivity.this, ProfileActivity.class));
                    return true;
                }

                return false;
            }
        });

        blogTitle = findViewById(R.id.blogTitle);
        blogContent = findViewById(R.id.blogContent);
        blogImage = findViewById(R.id.blogImage);
        selectImageButton = findViewById(R.id.selectImageButton);
        submitBlogButton = findViewById(R.id.submitBlogButton);

        selectImageButton.setOnClickListener(v -> openImageChooser());

        submitBlogButton.setOnClickListener(v -> {
            try {
                submitBlog();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                blogImage.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return base64String.replaceAll("\\s", "");
    }

    private void submitBlog() throws UnsupportedEncodingException {
        String title = blogTitle.getText().toString().trim();
        String content = blogContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty() || selectedImage == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }


        String base64Image = convertImageToBase64(selectedImage);

        System.out.println("String Base64 : "+base64Image);

        // Get the email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("demo", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        String username=sharedPreferences.getString("username","");
        System.out.println(email);

        if (email.isEmpty()) {
            Toast.makeText(this, "Email not found in SharedPreferences", Toast.LENGTH_SHORT).show();
            return;
        }

        String encodedTitle = URLEncoder.encode(title, "UTF-8");
        String encodedDescription = URLEncoder.encode(content, "UTF-8");

        String jsonPayload = String.format(
                "{\"email\":\"%s\", \"username\":\"%s\", \"title\":\"%s\", \"content\":\"%s\", \"image\":\"%s\"}",
                email, username, encodedTitle, encodedDescription, base64Image
        );

        System.out.println("JSON Payload: " + jsonPayload);
        // Send JSON payload to your API
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String urlString = "http://10.0.2.2:8000/api/createblog/";  // Replace with your actual API endpoint
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
                    runOnUiThread(() -> {
                        Toast.makeText(CreateBlogActivity.this, "Blog Added successfully!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateBlogActivity.this, "Blog submission failed. Response Code: " + responseCode, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(CreateBlogActivity.this, "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();


        Intent intent=new Intent(CreateBlogActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();



    }
}
