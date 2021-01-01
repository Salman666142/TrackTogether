package com.technicallskillz.tracktogether.Utills;

import java.util.HashMap;

public class User {
    private String email;
    private String username;
    private double Lat;
    private double Long;
    private String city;
    private String effected;
    private String status;
    private String profileImageUrl;

    public User(String email, String username, double lat, double aLong, String city, String effected, String status, String profileImageUrl) {
        this.email = email;
        this.username = username;
        Lat = lat;
        Long = aLong;
        this.city = city;
        this.effected = effected;
        this.status = status;
        this.profileImageUrl = profileImageUrl;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLong() {
        return Long;
    }

    public void setLong(double aLong) {
        Long = aLong;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEffected() {
        return effected;
    }

    public void setEffected(String effected) {
        this.effected = effected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
