package com.example.android.goforlunch.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by Diego Fajardo on 07/06/2018.
 */
public class AlertJobCreator implements JobCreator {

    @Nullable
    @Override
    public Job create(@NonNull String tag) {

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
