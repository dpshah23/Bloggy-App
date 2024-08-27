package com.example.bloggy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BlogAdapter blogAdapter;
    private List<Blog> blogList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        blogAdapter = new BlogAdapter(blogList, this);
        recyclerView.setAdapter(blogAdapter);

        // Fetch blog posts from API
        fetchBlogsFromApi();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the LogoutActivity
                Intent intent = new Intent(HomeActivity.this, LogoutActivity.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                    return true;
                } else if (itemId == R.id.create) {
                    startActivity(new Intent(HomeActivity.this, CreateBlogActivity.class));
                    return true;
                } else if (itemId == R.id.myblog) {
                    startActivity(new Intent(HomeActivity.this, MyBlogsActivity.class));
                    return true;
                } else if (itemId == R.id.profile) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void fetchBlogsFromApi() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8000/api/getblogs/"; // Replace with your API endpoint

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray blogsArray = jsonResponse.getJSONArray("data");

                        for (int i = 0; i < blogsArray.length(); i++) {
                            JSONObject blogObject = blogsArray.getJSONObject(i);
                            
                            System.out.println(blogObject);
                            
                            Blog blog = new Blog(
                                    blogObject.getString("content"),
                                    blogObject.getString("email"),
                                    blogObject.getString("id"),
                                    blogObject.getString("image"),
                                    blogObject.getString("timestamp"),
                                    blogObject.getString("title"),
                                    blogObject.getString("username")
                            );

                            blogList.add(blog);
                        }

                        // Update UI on the main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                blogAdapter.notifyDataSetChanged();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
