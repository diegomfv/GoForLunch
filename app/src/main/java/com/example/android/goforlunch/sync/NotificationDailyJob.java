package com.example.android.goforlunch.sync;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.example.android.goforlunch.R;
import com.example.android.goforlunch.activities.rest.RestaurantActivity;
import com.example.android.goforlunch.utils.UtilsGeneral;
import com.example.android.goforlunch.utils.UtilsFirebase;
import com.example.android.goforlunch.constants.Repo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by Diego Fajardo on 07/06/2018.
 */

/** This class is responsible of
 * the creation of the Notification
 * */
public class NotificationDailyJob extends DailyJob {

    public static final String TAG = "NotificationDailyJob";

    private static final int PENDING_INTENT_ID = 3147;

    private static final String NOTIFICATION_CHANNEL_ID = "notification_channel";

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase fireDb;
    private DatabaseReference fireDbRefUsers;

    private String userKey;

    private SharedPreferences sharedPref;

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        Log.d(TAG, "onRunDailyJob: called!");

        /* We check if notifications are enabled.
        If they are, we star the process to create a notification.
        If they are not, we do nothing.
        * */
        boolean notificationsAreEnabled = (sharedPref.getBoolean(getContext().getResources().getString(R.string.pref_key_notifications), false));

        if (notificationsAreEnabled) {

            /* We check if internet is available.
            * If it is available, we continue with the process (creating the notification)
            * If it is not, we stop the process */
            UtilsGeneral.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
                @Override
                public void onNext(Boolean aBoolean) {
                    Log.d(TAG, "onNext: ");

                    if (aBoolean) {
                        /* if aBoolean is true, there is internet connection.
                        * We continue with the process
                        * */

                        auth = FirebaseAuth.getInstance();
                        currentUser = auth.getCurrentUser();

                        /* We check if the user is currently sign up.
                        * If he is, we continue the process.
                        * If not, we stop the process*/
                        if (currentUser != null) {

                            fireDb = FirebaseDatabase.getInstance();
                            sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

                            userKey = sharedPref.getString(Repo.SharedPreferences.USER_ID_KEY, "");

                            fireDbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS)
                                    .child(userKey)
                                    .child(Repo.FirebaseReference.USER_RESTAURANT_INFO);
                            fireDbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                                    if (dataSnapshot.child(Repo.FirebaseReference.RESTAURANT_NAME).getValue().toString() != null &&
                                            dataSnapshot.child(Repo.FirebaseReference.RESTAURANT_ADDRESS).getValue().toString() != null) {

                                        /* If there is restaurant info available,
                                        * we create the notification
                                        * */
                                        createNotification(dataSnapshot.child(Repo.FirebaseReference.RESTAURANT_NAME).getValue().toString(),
                                                dataSnapshot.child(Repo.FirebaseReference.RESTAURANT_ADDRESS).getValue().toString());

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(TAG, "onCancelled: " + databaseError.getCode());

                                }
                            });
                        }

                    } else {
                        //do nothing, there is no internet connection
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "onError: " + e.getMessage() );
                }

                @Override
                public void onComplete() {
                    Log.d(TAG, "onComplete: ");
                }
            });

        } else {
            //do nothing since notifications are not enabled
        }

        return DailyJobResult.SUCCESS;
    }

    public static int scheduleNotificationDailyJob () {
        Log.d(TAG, "scheduleNotificationDailyJob: called!");

        return DailyJob.schedule(new JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(12),
                TimeUnit.HOURS.toMillis(13));

    }

    /*******************************
     * METHOD TO CREATE ************
     * THE NOTIFICATIONS ***********
     * ****************************/

    /** Method to create
     * the Notification
     */
    private void createNotification (final String title, final String address) {
        Log.d(TAG, "createNotification: called!");

        fireDbRefUsers = fireDb.getReference(Repo.FirebaseReference.USERS + "/" + userKey + "/" + Repo.FirebaseReference.USER_RESTAURANT_INFO);
        fireDbRefUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                NotificationManager notificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(
                            NOTIFICATION_CHANNEL_ID,
                            getContext().getString(R.string.notif_notification_channel_name),
                            NotificationManager.IMPORTANCE_HIGH);
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(mChannel);
                    }
                }

                /* We check if the user has chosen a restaurant.
                * If there is a restaurant linked to the user, we continue with the process.
                * If not, we stop the process
                * */
                if (dataSnapshot.child(Repo.FirebaseReference.RESTAURANT_NAME).getValue() != null) {
                    if (dataSnapshot.child(Repo.FirebaseReference.RESTAURANT_NAME).getValue().toString().equalsIgnoreCase("")) {
                        //do nothing because there is no restaurant chosen

                    } else {
                        /* There is a restaurant linked to the user, so we start creating the notification
                        * */

                        //When the user clicks the notification the app will be redirected to the activity
                        Intent intent = new Intent(getContext(), RestaurantActivity.class);

                        //We ensure that the activity will be replaced if needed
                        // even though the activity was already open or running in the background
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        /* We fill the intent with the restaurant information
                         * */
                        Map<String, Object> map = UtilsFirebase.fillMapWithRestaurantInfoUsingDataSnapshot(dataSnapshot);
                        UtilsGeneral.fillIntentUsingMapInfo(intent, map);

                        //Pending intent creation
                        PendingIntent pendingIntent = PendingIntent.getActivity(
                                getContext(),
                                PENDING_INTENT_ID,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        //The request code must be the same as the same we pass to .notify later
                        NotificationCompat.Builder notificationBuilder =
                                new NotificationCompat.Builder(getContext(), NOTIFICATION_CHANNEL_ID)
                                        .setContentIntent(pendingIntent)
                                        .setSmallIcon(R.drawable.go_for_lunch_icon)
                                        .setContentTitle(title)
                                        .setContentText(address)
                                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                                        .setAutoCancel(true);
                        //SetAutoCancel(true) makes the notification dismissible when the user swipes it away

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
                        }

                        //Request code must be the same as the pending intent
                        if (notificationManager != null) {
                            notificationManager.notify(100, notificationBuilder.build());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getCode());

            }
        });
    }
}
