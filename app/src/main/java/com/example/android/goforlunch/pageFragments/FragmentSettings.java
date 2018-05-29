package com.example.android.goforlunch.pageFragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.example.android.goforlunch.R;

/**
 * Created by Diego Fajardo on 07/05/2018.
 */

public class FragmentSettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_go_for_lunch);

    }
}
