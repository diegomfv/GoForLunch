package com.example.android.goforlunch;

import android.app.Application;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.AppExecutors;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.sync.AlertJobCreator;

/**
 * Created by Diego Fajardo on 07/06/2018.
 */
public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: called!");
        JobManager.create(this).addJobCreator(new AlertJobCreator());


    }
}