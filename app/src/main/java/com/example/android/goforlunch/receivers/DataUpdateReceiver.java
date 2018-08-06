package com.example.android.goforlunch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.goforlunch.constants.Repo;
import com.example.android.goforlunch.rx.ObservableObject;

/**
 * Created by Diego Fajardo on 06/08/2018.
 */
public class DataUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = DataUpdateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: called!");

        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(Repo.SentIntent.LOAD_DATA_IN_VIEWMODEL)) {

                //2: started; 3: finished
                int fetchingProcessStage = intent.getIntExtra(Repo.SentIntent.FETCHING_PROCESS_STAGE, 3);
                ObservableObject.getInstance().update(fetchingProcessStage);
            }
        }
    }
}
