package com.example.android.goforlunch.utils;

import com.example.android.goforlunch.network.models.placebyid.Result;
import com.example.android.goforlunch.constants.Repo;

import java.util.Calendar;

/**
 * Created by Diego Fajardo on 21/06/2018.
 */
public class UtilsRemote {

    private static final String TAG = UtilsRemote.class.getSimpleName();

    private static int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

    public static String checkClosingTime (Result result) {

        if (result.getOpeningHours() != null) {
            if (result.getOpeningHours().getPeriods() != null
                    && result.getOpeningHours().getPeriods().size() > 0) {

                for (int i = 0; i < result.getOpeningHours().getPeriods().size(); i++) {

                    if (result.getOpeningHours().getPeriods().get(i).getClose() != null) {

                        if (result.getOpeningHours().getPeriods().get(i).getClose().getDay() != null
                                && result.getOpeningHours().getPeriods().get(i).getClose().getDay().toString().equalsIgnoreCase(String.valueOf(day))) {

                            return UtilsGeneral.formatTime(result.getOpeningHours().getPeriods().get(i).getClose().getTime());
                        }

                    }

                } return Repo.NOT_AVAILABLE_FOR_STRINGS;
            } else return Repo.NOT_AVAILABLE_FOR_STRINGS;
        } else return Repo.NOT_AVAILABLE_FOR_STRINGS;
    }
}
