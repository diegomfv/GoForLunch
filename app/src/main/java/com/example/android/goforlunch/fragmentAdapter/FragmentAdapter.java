package com.example.android.goforlunch.fragmentAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.example.android.goforlunch.pageFragments.FragmentCoworkersView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantListView;
import com.example.android.goforlunch.pageFragments.FragmentRestaurantMapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class FragmentAdapter extends FragmentPagerAdapter {

    List<Fragment> listOfFragments = new ArrayList<>();

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }


    // TODO: 27/04/2018 Take care. We create a list but later we don't use it here
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new FragmentRestaurantMapView();
            case 1:
                return new FragmentRestaurantListView();
            case 2:
                return new FragmentCoworkersView();
            default:
                return null;

            }

        }

    @Override
    public int getCount() {
        return listOfFragments.size();
    }

    public void addFragment (Fragment fragment) {
        listOfFragments.add(fragment);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

    }

}
