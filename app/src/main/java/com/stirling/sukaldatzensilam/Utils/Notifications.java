package com.stirling.sukaldatzensilam.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.stirling.sukaldatzensilam.R;

public class Notifications
{
    public static void show(Context ctx, Class caller, String title, String message){

        NotificationManager notificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(ctx, caller);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            //Show notification that service has been started

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            int requestCode = 0;
            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, requestCode, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(ctx, "")
                    .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(),
                            R.mipmap.ic_launcher))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setTicker(title + " " + message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            notificationManager.notify(0, noBuilder.build()); //0 = ID of notification
        }
        else
        {
            int notificationId = 1;
            String channelId = "channel-01";
            String channelName = "Channel Name";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        channelId, channelName, importance);
                notificationManager.createNotificationChannel(mChannel);
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            mBuilder.setContentIntent(resultPendingIntent);

            notificationManager.notify(notificationId, mBuilder.build());
        }


    }
}
