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

                //Data 2 means notify MainActivity Fetching Process Ended
                ObservableObject.getInstance().update(2);
            }
        }
    }
}
