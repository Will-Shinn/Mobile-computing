package com.mobile.daryldaryl.mobile_computing.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START blog_user_class]
@IgnoreExtraProperties
public class Checkin {

    private String userId;
    private String username;
    private Place place;
    private String time;

    public Checkin() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Checkin(String userId, String username, Place place,String time) {
        this.userId = userId;
        this.username = username;
        this.place = place;
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", username);
        result.put("place", place);
        result.put("time", time);

        return result;
    }
}
// [END blog_user_class]
