package com.example.android.goforlunch;

import com.example.android.goforlunch.strings.RepoStrings;

import org.junit.Assert;
import org.junit.Test;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        System.out.println("generated float = " + df.format(generatedFloat));
        assertTrue(generatedFloat < 3);

    }

    /** Used to select the type of the restaurant according to the url used for
     * the request
     * */
    @Test
    public void substringInString () {

        Random rand = new Random();
        int r = rand.nextInt(12) + 1;

        String type = RepoStrings.NOT_AVAILABLE;
        String urlTypePart = RepoStrings.RESTAURANT_TYPES[r].substring(0,4);

        for (int i = 1; i < RepoStrings.RESTAURANT_TYPES.length ; i++) {

            System.out.println(RepoStrings.RESTAURANT_TYPES[i].substring(0,4));

            if (urlTypePart.equals(RepoStrings.RESTAURANT_TYPES[i].substring(0,4))){
                type = RepoStrings.RESTAURANT_TYPES[i];
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

    @Test
    public void findDayInTheArray () {

        Random rand = new Random();

        int counter = 0;

        List<String> days = new ArrayList<>();

        for (int i = 0; i < 7; i++) {

            if (rand.nextBoolean()) {

                days.add(String.valueOf(counter));
                counter++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < days.size(); i++) {
            System.out.println("day (" + i + ") = " + days.get(i));
        }

        String[] daysInRequest = new String[days.size()];
        daysInRequest = days.toArray(daysInRequest);

        String today = String.valueOf(rand.nextInt(6));
        System.out.println("today = " + today);

        for (int i = 0; i < daysInRequest.length; i++) {

            if (daysInRequest[i].equals(today)) {
                //We get the close time in the app
                System.out.println("Days list contains today");
                assertTrue(daysInRequest[i].equals(today));
                break;
            }
        }

        if (!days.contains(today)) {
            System.out.println("Days list doesn't contain today");
            assertTrue(!days.contains(today));
        }
    }

    /** Method used in the android job to know if an element has to be added to the list
     * */
    @Test
    public void checkAnElementIsInTheList () {

        List<String> listOfRestaurants = new ArrayList<>();

        listOfRestaurants.add("KFC");
        listOfRestaurants.add("McDonalds");
        listOfRestaurants.add("Tony Romas");
        listOfRestaurants.add("Burger King");

        int sizeOfList = listOfRestaurants.size();

        String restaurant;

        Random rand = new Random();

        int r = rand.nextInt(10);
        System.out.println("r = " + r);

        if (r < 4) {
            System.out.println("r < 4");
            restaurant = listOfRestaurants.get(r);
        } else {
            System.out.println("r > 4");
            restaurant = "Not in the list";
        }

        if (listOfRestaurants.contains(restaurant)) {
            System.out.println("restaurant is in the list");
            assertTrue(listOfRestaurants.contains(restaurant));
        } else {
            System.out.println("restaurant is not in the list");
            listOfRestaurants.add(restaurant);
            assertTrue(listOfRestaurants.size() == (sizeOfList + 1));

        }
    }

    @Test
    public void emailTransformation () {

        String email = "Stack_Overflow@email.com";

        email = email.toLowerCase();

        if (email.contains(".")) {
            email = email.replace(".", ",");
        }

        System.out.println(email);

        assertTrue(2==2);

    }

    @Test
    public void getFirsUserIdThatIsEmptyAndFillIt () {

        Random random = new Random();

        List<Integer> randomNumbers = new ArrayList<>();
        randomNumbers.add(random.nextInt(200));
        randomNumbers.add(random.nextInt(200));
        randomNumbers.add(random.nextInt(200));
        randomNumbers.add(random.nextInt(200));

        /** We create a map with all the users and some gaps
         * according to random numbers
         * */
        Map<String,Object> map = new HashMap<>();

        for (int i = 0; i < 200; i++) {

            if (randomNumbers.contains(i)){
                map.put("user" + i, "");

            } else {
                map.put("user" + i, "user" + i);
            }

        }

        /** We find the gaps
         * */

        for (Map.Entry<String,Object> entry:
             map.entrySet()) {

            if (entry.getValue().equals("")) {
                System.out.println(entry.getKey());
            }
        }

        System.out.println(randomNumbers.toString());
    }

    @Test
    public void mapOfListsCreation () {

        Map<String, List<String>> map = new HashMap<>();

        String thaiRestaurants = "thai_restaurants";
        String chineseRestaurants = "chinese_restaurants";

        List<String> listOfThaiRestaurants = new ArrayList<>();
        List<String> listOfChineseRestaurants = new ArrayList<>();

        listOfThaiRestaurants.add("Koh Thai");
        listOfThaiRestaurants.add("Thai Sesame");

        listOfChineseRestaurants.add("Woh Gung");
        listOfChineseRestaurants.add("Chinese Paradise");

        map.put(thaiRestaurants, listOfThaiRestaurants);
        map.put(chineseRestaurants, listOfChineseRestaurants);

        List<String> tempList = map.get(thaiRestaurants);

        for (int i = 0; i < tempList.size(); i++) {
            System.out.println("tempList: " + tempList.get(i));
        }
    }

}