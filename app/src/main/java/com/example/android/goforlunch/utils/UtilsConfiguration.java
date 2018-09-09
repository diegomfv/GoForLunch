package com.example.android.goforlunch.utils;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.android.goforlunch.R;

/**
 * Created by Diego Fajardo on 25/06/2018.
 */
public class UtilsConfiguration {

    public static void configureActionBar (Context context, Toolbar toolbar) {

        if (((AppCompatActivity) context) != null) {
            ((AppCompatActivity) context).setSupportActionBar(toolbar);
        }

        if (((AppCompatActivity) context) != null) {
            ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

    }




}
