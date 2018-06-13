package com.example.android.goforlunch.job;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;

import java.util.concurrent.TimeUnit;

/**
 * Created by Diego Fajardo on 07/06/2018.
 */
public class NotificationDailyJob extends DailyJob {

    public static final String TAG = "NotificationDailyJob";

    private static final int PENDING_INTENT_ID = 3147;

    private static final String NOTIFICATION_CHANNEL_ID = "notification_channel";


    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {




        
        return null;
    }



    public static int scheduleNotificationDailyJob () {

        return DailyJob.schedule(new JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(12),
                TimeUnit.HOURS.toMillis(13));

    }

    /*******************************
     * METHOD TO CREATE ************
     * THE NOTIFICATIONS ***********
     * ****************************/

//    /** Method to create
//     * the Pending Intent
//     */
//    private PendingIntent contentIntent (Context context) {
//
//        // TODO: 07/06/2018 Send the user to the RestaurantActivity filling the intent with user info, as in MainActivity
//        //When the user clicks the notification the app will be redirected to the activity
//        Intent intent = new Intent(context, RestaurantActivity.class);
//
//        //We ensure that the activity will be replaced if needed, although the activity
//        // was already open or running in the background
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        return PendingIntent.getActivity(
//                context,
//                PENDING_INTENT_ID,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//    }
//
//    /** Method to create
//     * the Notification
//     */
//    private void createNotification() {
//
//        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(
//                Context.NOTIFICATION_SERVICE);
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel mChannel = new NotificationChannel(
//                    NOTIFICATION_CHANNEL_ID,
//                    getContext().getString(R.string.main_notification_channel_name),
//                    NotificationManager.IMPORTANCE_HIGH);
//            if (notificationManager != null) {
//                notificationManager.createNotificationChannel(mChannel);
//            }
//        }
//
//        //The request code must be the same as the same we pass to .notify later
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(getContext(), NOTIFICATION_CHANNEL_ID)
//                        .setContentIntent(contentIntent(getContext()))
//                        .setSmallIcon(android.R.drawable.ic_menu_sort_by_size)
//                        .setContentTitle(getContext().getResources().getString(R.string.notification_title))
//                        .setContentText(getContext().getResources().getString(R.string.notification_message))
//                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
//                        .setAutoCancel(true);
//        //SetAutoCancel(true) makes the notification dismissible when the user swipes it away
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
//                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
//            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
//        }
//
//        //Request code must be the same as the pending intent
//        if (notificationManager != null) {
//            notificationManager.notify(100, notificationBuilder.build());
//        }
//    }

}
