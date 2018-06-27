package com.example.android.goforlunch.helpermethods;

import android.util.Log;

import com.example.android.goforlunch.remote.newmodels.placebyid.Result;
import com.example.android.goforlunch.repository.RepoStrings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Calendar;

/**
 * Created by Diego Fajardo on 21/06/2018.
 */
public class UtilsRemote {

    private static final String TAG = UtilsRemote.class.getSimpleName();

    private static int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

    public static String checkClosingTime (Result result) {

        if (null != result.getOpeningHours()) {
            if (result.getOpeningHours().getPeriods().size() > 0) {

                for (int i = 0; i < result.getOpeningHours().getPeriods().size(); i++) {

                    if (result.getOpeningHours().getPeriods().get(i).getClose().getDay().toString()
                            .equalsIgnoreCase(String.valueOf(day))) {
                        return Utils.formatTime(result.getOpeningHours().getPeriods().get(i).getClose().getTime());
                    }

                } return RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
            } else return RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
        } else return RepoStrings.NOT_AVAILABLE_FOR_STRINGS;
    }
}
