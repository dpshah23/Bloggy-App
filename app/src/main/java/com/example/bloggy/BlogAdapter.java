package com.example.bloggy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {
    private List<Blog> blogList;
    private Context context;

    public BlogAdapter(List<Blog> blogList, Context context) {
        this.blogList = blogList;
        this.context = context;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.blog_item, parent, false);

        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blog blog = blogList.get(position);

        try {
            holder.title.setText(blog.getTitle());
            holder.content.setText(blog.getContent());

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        System.out.println("URL image : "+blog.getImage());
        System.out.println("Time sTamp "+blog.getTimestamp());

        // Load image with Glide, using placeholder and error handling
        Glide.with(context)
                .load(blog.getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);


        holder.username.setText(blog.getUsername());
        holder.timestamp.setText(blog.getTimestamp());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, detailblog.class);
            intent.putExtra("BLOG_ID", blog.getId()); // Pass the blog ID to the new activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, username, timestamp;
        ImageView imageView;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewTitle);
            content = itemView.findViewById(R.id.textViewDescription);
            username = itemView.findViewById(R.id.username);
            timestamp = itemView.findViewById(R.id.blogTimestamp);
            imageView = itemView.findViewById(R.id.blogsImage);
        }
    }
}
