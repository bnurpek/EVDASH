package com.example.myapplication.database;

import java.util.Date;

public class Notifications {
    private static int count = 0;
    private String id;
    public String title;
    public String body;
    public String senderToken;
    public String senderID;
    public Boolean answered;

    public Notifications() {}

    public Notifications(String title, String body, String senderToken, String senderID, Boolean answered){
        this.title = title;
        this.body = body;
        this.senderID = senderID;
        this.senderToken = senderToken;
        this.answered = answered;
        id = String.valueOf(count);
        count++;
    }

    public String getId() {
        return id;
    }
}
