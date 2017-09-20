package com.mobile.daryldaryl.mobile_computing.tools;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class DisplayUtils {

    public static int getScreenWidth(Context context){
        DisplayMetrics  dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
