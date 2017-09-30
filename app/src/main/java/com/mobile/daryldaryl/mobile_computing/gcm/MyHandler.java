package com.mobile.daryldaryl.mobile_computing.gcm;

/**
 * Created by liboa on 29/09/2017.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.microsoft.windowsazure.notifications.NotificationsHandler;
import com.mobile.daryldaryl.mobile_computing.MainActivity;
import com.mobile.daryldaryl.mobile_computing.R;

public class MyHandler extends NotificationsHandler {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Context ctx;
    boolean flag = true;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        double lat = Double.parseDouble(bundle.getString("lat"));
        double lng = Double.parseDouble(bundle.getString("lng"));
        String type = bundle.getString("type");
        switch (type) {
            case "add":
                if (flag) {
                    flag = false;
                    MainActivity.addCheckIn(new LatLng(lat, lng), MainActivity.mProvider, MainActivity.mOverlay);

                } else {
                    flag = true;
                    MainActivity.addCheckIn(new LatLng(lat, lng), MainActivity.mProviderdb, MainActivity.mOverlaydb);
                }
                break;
            case "remove":
                MainActivity.removeCheckIn();
                break;
        }
        sendNotification(type + " -- " + "(" + lat + ", " + lng + ")");
        if (MainActivity.isVisible) {
//            MainActivity.mainActivity.ToastNotify(nhMessage);
            Toast.makeText(ctx, type + " -- " + "(" + lat + ", " + lng + ")", Toast.LENGTH_LONG).show();

        }
    }

    private void sendNotification(String msg) {

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Notification Hub Demo")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setSound(defaultSoundUri)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}