package com.example.android.goforlunch.sync;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by Diego Fajardo on 07/06/2018.
 */
public class AlertJobCreator implements JobCreator {

    private static final String TAG = AlertJobCreator.class.getSimpleName();

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        Log.d(TAG, "create: called");

        switch (tag) {

            case NotificationDailyJob.TAG: {
                return new NotificationDailyJob();
            }

            case AddRestaurantToGroupDailyJob.TAG: {
                return new AddRestaurantToGroupDailyJob();
            }

            default: {
                return null;
            }
        }
    }
}
