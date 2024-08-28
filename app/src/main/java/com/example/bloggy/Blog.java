package com.example.bloggy;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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
        this.timestamp = timestamp;
    }
    public String getUsername(){
        return username;
    }
    public String getTitle() throws UnsupportedEncodingException {
        String decodedData = URLDecoder.decode(title, "UTF-8");
        return decodedData;
    }
    public String getEmail() {
        return email;
    }
    public String getId() {
        return id;
    }
    public String getContent() throws UnsupportedEncodingException {

        String decodedData = URLDecoder.decode(content, "UTF-8");


        return decodedData;
    }
    public String getImage() {
        return image;
    }
    public String getTimestamp() {
        return timestamp;
    }
}

