package com.example.android.goforlunch.helpermethods;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;

/**
 * Created by Diego Fajardo on 27/04/2018.
 */

public class Utils {

    public static void menuIconColor (MenuItem menuItem, int color) {
        Drawable drawable = menuItem.getIcon();
        drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

}
