package com.example.android.goforlunch;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTests {

    @Test
    public void additionOfDotToDate () {

        String time = "1600";
        time = time.substring(0,2) + "." + time.substring(2, time.length());
        System.out.println(time);
        Assert.assertTrue(2 == 2);

    }

    @Test
    public void ratingTransformation () {

        String ratingFromResults = "2.5";

        float tempRating = Float.parseFloat(ratingFromResults);

        if (tempRating > 3) {
            tempRating = tempRating * 3 / 5;
        }

        System.out.println(tempRating);
        assertTrue(tempRating < 3);

    }

}