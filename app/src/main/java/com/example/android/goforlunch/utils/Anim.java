package com.example.android.goforlunch.utils;

import android.view.View;

/**
 * Created by Diego Fajardo on 06/05/2018.
 */

public class Anim {

    /** Retrieve and cache the system's default "short, medium and long" animation time (200,400,500)
     shortAnimationDuration = getResources().getInteger(
            android.R.integer.config_shortAnimTime);
     MediumAnimationDuration = getResources().getInteger(
            android.R.integer.config_mediumAnimTime);
     LongAnimationDuration = getResources().getInteger(
            android.R.integer.config_longAnimTime);
     */

    private static int shortAnim = 200;
    private static int mediumAnim = 400;
    private static int longAnim = 500;

    public static void crossFadeShortAnimation(View view) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        view.animate()
                .alpha(1f)
                .setDuration(shortAnim)
                .setListener(null);

    }

    public static void crossFadeMediumAnimation(View view) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        view.animate()
                .alpha(1f)
                .setDuration(mediumAnim)
                .setListener(null);

    }

    public static void crossFadeLongAnimation(View view) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        view.animate()
                .alpha(1f)
                .setDuration(longAnim)
                .setListener(null);

    }

}
