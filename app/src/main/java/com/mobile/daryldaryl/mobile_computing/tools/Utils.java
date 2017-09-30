package com.mobile.daryldaryl.mobile_computing.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by liboa on 24/09/2017.
 */

public class Utils {

    public static String parseTime(long target_time) {
        String dateFormat = "E MMM dd, yyyy hh:mm aa";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(target_time);

        return simpleDateFormat.format(calendar.getTime());
    }

    public static String parseShortTime(long target_time) {
        String dateFormat = "MMM dd, yyyy HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(target_time);

        return simpleDateFormat.format(calendar.getTime());
    }

    public static String parseRecentTime(long target_time) {
        long current_time = Calendar.getInstance().getTimeInMillis();


        long recent = current_time - target_time;

        long minutes = recent / 1000 / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (minutes < 1) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " mins ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days < 30) {
            return days + " days ago";
        } else {
            return parseShortTime(target_time);
        }
    }
}
