package com.example.bloggy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private BlogAdapter blogAdapter;
    private List<Blog> blogList = new ArrayList<>();
    private boolean isLoading = false;
    private int pageNumber = 1;
    private int totalPages = 1;  // Assuming the API returns this value


    private void refreshBlogs() {
        pageNumber = 1;
        blogList.clear();
        fetchBlogsFromApi(pageNumber);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);

        boolean isConnected = NetworkUtil.isConnectedToInternet(this);

        if (NetworkUtil.isConnectedToInternet(this)) {

        } else {

            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform the refresh operation here
                refreshBlogs();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        blogAdapter = new BlogAdapter(blogList, this);
        recyclerView.setAdapter(blogAdapter);

        // Fetch the initial set of blogs
        fetchBlogsFromApi(pageNumber);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && pageNumber < totalPages) {
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount * 0.7) {
                        pageNumber++;
                        fetchBlogsFromApi(pageNumber);
                    }
                }
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

    private void fetchBlogsFromApi(int page) {
        isLoading = true;
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8000/api/getblogs/?page=" + page; // Assuming the API supports pagination

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                isLoading = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray blogsArray = jsonResponse.getJSONArray("data");
                        totalPages = jsonResponse.getInt("total_pages");  // Get the total pages from the API response

                        for (int i = 0; i < blogsArray.length(); i++) {
                            JSONObject blogObject = blogsArray.getJSONObject(i);

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
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        isLoading = false;
                    }
                } else {
                    isLoading = false;
                }
            }
        });
    }
}
