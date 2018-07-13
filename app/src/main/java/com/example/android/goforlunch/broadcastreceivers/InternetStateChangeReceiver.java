package com.example.android.goforlunch.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.helpermethods.Utils;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by Diego Fajardo on 13/07/2018.
 */

/** Broadcast receiver that listens to internet state changes
 * */
public class InternetStateChangeReceiver extends BroadcastReceiver {

    private static final String TAG = InternetStateChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG, "onReceive: called!");

        Utils.checkInternetInBackgroundThread(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                Log.d(TAG, "onNext: called!");

                if (!aBoolean) {

                    ObservableObject.getInstance().update(0);
//                    ToastHelper.toastShort(context, "There is no internet");

                } else {

                    ObservableObject.getInstance().update(1);

//                    ToastHelper.toastShort(context, "Internet Available");

                }

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: called!");

            }
        });

    }
}
