package com.example.myapplication.database;

import java.io.Serializable;

public class User implements Serializable {
    private String profilePhotoUrl;
    private String uID;
    private String email, notificationToken, phone, address;
    private String fullName, department, grade, state, day, km;
    private boolean phoneStatue, mailStatue;
    private double latitude, longitude;

    public User(){}

    public User(String path, String uid, String notificationToken, String email, String str_fullname, String str_phone,
                String str_department, String str_grade, String str_state, String str_day,
                String str_adr, String str_km, Boolean phoneStatue, Boolean mailStatue,
                double latitude, double longitude){
        profilePhotoUrl = path;     uID = uid;
        this.email = email;         fullName = str_fullname;
        phone = str_phone;          department = str_department;
        grade = str_grade;          state = str_state;
        day = str_day;              km = str_km;
        address = str_adr;            this.phoneStatue = phoneStatue;
        this.mailStatue = mailStatue;   this.notificationToken = notificationToken;
        this.latitude = latitude;       this.longitude = longitude;
    }

    public String getProfilePhotoUrl() {return profilePhotoUrl;}
    public String getuID() {return uID;}
    public String getEmail() {return email;}
    public String getPhone() {return phone;}
    public String getFullName() {return fullName;}
    public String getDepartment() {return department;}
    public String getGrade() {return grade;}
    public String getState() {return state;}
    public String getDay() {return day;}
    public String getKm() {return km;}
    public boolean isPhoneStatue() {return phoneStatue;}
    public boolean isMailStatue() {return mailStatue;}
    public String getAddress() {return address;}
    public String getNotificationToken() {return notificationToken;}
    public double getLatitude() {return latitude;}
    public double getLongitude() {return longitude;}

    public void setPhoneStatue(boolean phoneStatue) {this.phoneStatue = phoneStatue;}
    public void setMailStatue(boolean mailStatue) {this.mailStatue = mailStatue;}
}
