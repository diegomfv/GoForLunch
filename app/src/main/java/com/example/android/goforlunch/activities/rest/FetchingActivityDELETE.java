package com.example.android.goforlunch.activities.rest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.sync.FetchingService;

/**
 * Created by Diego Fajardo on 11/07/2018.
 */
public class FetchingActivityDELETE extends AppCompatActivity {

    private static final String TAG = FetchingActivityDELETE.class.getSimpleName();

    private String[] arrayOfTypes;

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fetching_info_activity);

        arrayOfTypes = getResources().getStringArray(R.array.typesOfRestaurants);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: called!");

                startService(new Intent(FetchingActivityDELETE.this, FetchingService.class));

            }
        });















    }






}
