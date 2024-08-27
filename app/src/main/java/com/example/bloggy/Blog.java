package com.example.bloggy;

public class  Blog {
    private String email;
    private String id;
    private String timestamp;
    private String title;
    private String content;
    private String image;
    private String username;

    // Constructor, getters, and setters
    public Blog(String content, String email, String id, String image, String timestamp, String title, String username) {
        this.title = title;
        this.content = content;
        this.image = image;
        this.username = username;
        this.email = email;
        this.id = id;
        this.timestamp = this.timestamp;
    }
    public String getUsername(){
        return username;
    }
    public String getTitle() {
        return title;
    }
    public String getEmail() {
        return email;
    }
    public String getId() {
        return id;
    }
    public String getContent() {
        return content;
    }
    public String getImage() {
        return image;
    }
    public String getTimestamp() {
        return timestamp;
    }
}

