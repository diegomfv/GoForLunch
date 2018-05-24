package com.example.android.goforlunch;

import com.example.android.goforlunch.strings.StringValues;

import org.junit.Assert;
import org.junit.Test;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTests {

    /** Used to add a dot to the time to display the time properly
     * in the user interface */
    @Test
    public void additionOfDotToDate () {

        Random rand = new Random();

        int hour = rand.nextInt(23) + 10;
        int minute = rand.nextInt(59) + 10;

        String time = String.valueOf(hour) + String.valueOf(minute);

        time = time.substring(0,2) + "." + time.substring(2, time.length());

        System.out.println(time);
        System.out.println(time.charAt(2));
        Assert.assertTrue(Character.toString(time.charAt(2)).matches("."));

    }

    /** Used to transform the rating into
     * a string and push it to the database
     * */
    @Test
    public void ratingTransformation () {

        float leftLimit = 0.1f;
        float rightLimit = 5f;
        float generatedFloat = leftLimit + new Random().nextFloat() * (rightLimit - leftLimit);

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);

        if (generatedFloat > 3) {
            generatedFloat = generatedFloat * 3 / 5;
        }

        System.out.println(df.format(generatedFloat));
        assertTrue(generatedFloat < 3);

    }

    /** Used to select the type of the restaurant according to the url used for
     * the request
     * */
    @Test
    public void substringInString () {

        Random rand = new Random();
        int r = rand.nextInt(12) + 1;

        String type = StringValues.NOT_AVAILABLE;
        String urlTypePart = StringValues.RESTAURANT_TYPES[r].substring(0,4);

        for (int i = 1; i < StringValues.RESTAURANT_TYPES.length ; i++) {

            System.out.println(StringValues.RESTAURANT_TYPES[i].substring(0,4));

            if (urlTypePart.equals(StringValues.RESTAURANT_TYPES[i].substring(0,4))){
                type = StringValues.RESTAURANT_TYPES[i];
            }
        }

        System.out.println("type = " + type);
        System.out.println("urlTypePart = " + urlTypePart);
        assertTrue(type.contains(urlTypePart.substring(0,3)));

    }

    @Test
    public void separateNameAndSurname () {

        Random rand = new Random();
        int firstname = rand.nextInt(12) + 1;
        int lastname = rand.nextInt(12) + 1;

        String name = String.valueOf(firstname) + " " + String.valueOf(lastname);

        String[] nameParts = name.split(" ");

        System.out.println(firstname);
        System.out.println(lastname);

        assertTrue(nameParts[0].equals(String.valueOf(firstname)));
        assertTrue(nameParts[1].equals(String.valueOf(lastname)));
    }



}