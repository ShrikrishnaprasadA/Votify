package com.example.collegeelectionsystem;

import com.google.firebase.Timestamp;

public class Announcement {
    private String title;
    private String type;
    private String content;
    private Timestamp timestamp;

    public Announcement() {}

    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setTitle(String title) { this.title = title; }
    public void setType(String type) { this.type = type; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
