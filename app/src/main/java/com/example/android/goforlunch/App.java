package com.example.android.goforlunch;

import android.app.Application;

import com.evernote.android.job.JobManager;
import com.example.android.goforlunch.constants.Repo;
import com.example.android.goforlunch.sync.AlertJobCreator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

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
