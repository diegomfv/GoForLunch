package com.example.android.goforlunch.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.example.android.goforlunch.R;
import com.example.android.goforlunch.helpermethods.ToastHelper;
import com.example.android.goforlunch.pageFragments.FragmentCoworkersView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantListView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantMapView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigationView = findViewById(R.id.bottom_navigation_id);
        navigationView.setOnNavigationItemSelectedListener(botNavListener);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_id);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view_id);
        mNavigationView.setNavigationItemSelectedListener(navViewListener);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_id, FragmentRestaurantMapView.newInstance())
                .commit();

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*****************
     * LISTENERS *****
     * **************/

    private BottomNavigationView.OnNavigationItemSelectedListener botNavListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {

                        case R.id.nav_view_map_id:
                            selectedFragment = FragmentRestaurantMapView.newInstance();
                            break;
                        case R.id.nav_view_list_id:
                            selectedFragment = FragmentRestaurantListView.newInstance();
                            break;
                        case R.id.nav_view_coworkers_id:
                            selectedFragment = FragmentCoworkersView.newInstance();
                            break;
                    }

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_id, selectedFragment)
                            .commit();

                    //true means that we want to select the clicked item
                    //if we choose false, the fragment will be shown but the item
                    //won't be selected
                    return true;
                }
            };

    private NavigationView.OnNavigationItemSelectedListener navViewListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){

                        case R.id.nav_lunch: {
                            Log.d(TAG, "onNavigationItemSelected: lunch pressed");

                            startActivity(new Intent(MainActivity.this, RestaurantActivity.class));

                            return true;
                        }

                        case R.id.nav_settings: {
                            Log.d(TAG, "onNavigationItemSelected: settings pressed");

                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

                            return true;
                        }

                        case R.id.nav_logout: {
                            Log.d(TAG, "onNavigationItemSelected: log out pressed");

                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            boolean notif = sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_notifications), false);

                            ToastHelper.toastShort(MainActivity.this, String.valueOf(notif));

                            startActivity(new Intent(MainActivity.this, FirebaseActivity.class));

                            return true;
                        }

                    }

                    item.setChecked(true);

                    return true;
                }
            };

    /** Getter used to get the NavigationDrawer from inside a fragment
     * */
    public DrawerLayout getMDrawerLayout() {
        return mDrawerLayout;
    }
}

/**
 * android:id="@+id/nav_camera"
 android:icon="@drawable/ic_lunch"
 android:title="YOUR LUNCH" />
 <item
 android:id="@+id/nav_gallery"
 android:icon="@drawable/ic_settings"
 android:title="SETTINGS" />
 <item
 android:id="@+id/nav_slideshow"
 android:icon="@drawable/ic_logout"
 android:title="LOG OUT" />
 * **/



















