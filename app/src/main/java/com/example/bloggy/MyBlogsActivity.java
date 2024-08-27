package com.example.bloggy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyBlogsActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private BlogAdapter blogAdapter;
    private List<Blog> blogList = new ArrayList<>();
    private boolean isLoading = false;
    private int pageNumber = 1;
    private int totalPages = 1;  // Assuming the API returns this value
    private TextView noBlogsMessage;

    private void refreshBlogs() {
        pageNumber = 1;
        blogList.clear();
        fetchBlogsFromApi(pageNumber);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_blogs);

        noBlogsMessage = findViewById(R.id.noBlogsMessage);
        swipeRefreshLayout=findViewById(R.id.swipeRefresh);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnav);
        bottomNavigationView.setSelectedItemId(R.id.myblog);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        blogAdapter = new BlogAdapter(blogList, this);
        recyclerView.setAdapter(blogAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform the refresh operation here
                refreshBlogs();
            }
        });

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

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    startActivity(new Intent(MyBlogsActivity.this, HomeActivity.class));
                    return true;
                } else if (itemId == R.id.create) {
                    startActivity(new Intent(MyBlogsActivity.this, CreateBlogActivity.class));
                    return true;
                } else if (itemId == R.id.myblog) {
                    startActivity(new Intent(MyBlogsActivity.this, MyBlogsActivity.class));
                    return true;
                } else if (itemId == R.id.profile) {
                    startActivity(new Intent(MyBlogsActivity.this, ProfileActivity.class));
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
    }

    private void fetchBlogsFromApi(int page) {
        isLoading = true;
        OkHttpClient client = new OkHttpClient();

        SharedPreferences hred = getSharedPreferences("demo", MODE_PRIVATE);
        String username = hred.getString("username", "");
        String url = String.format("http://10.0.2.2:8000/api/getuserblog/%s/?page=%d", username, page); // Assuming the API supports pagination

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                isLoading = false;
                runOnUiThread(() -> {
                    // Stop refreshing animation if needed
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray blogsArray = jsonResponse.getJSONArray("data");
                        totalPages = jsonResponse.getInt("total_pages");  // Get the total pages from the API response

                        runOnUiThread(() -> {
                            if (blogsArray.length() == 0 && pageNumber == 1) {
                                // Show no blogs message and hide recyclerView if it's the first page
                                recyclerView.setVisibility(View.GONE);
                                noBlogsMessage.setVisibility(View.VISIBLE);
                                swipeRefreshLayout.setRefreshing(false);
                            } else {
                                // Hide no blogs message and show recyclerView
                                noBlogsMessage.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        });

                        if (blogsArray.length() > 0) {
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

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    blogAdapter.notifyDataSetChanged();
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            });

                        }
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
