package com.example.android.goforlunch.application;

import android.app.Application;

import com.evernote.android.job.JobManager;
import com.example.android.goforlunch.job.AlertJobCreator;

/**
 * Created by Diego Fajardo on 07/06/2018.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new AlertJobCreator());

    }
}
