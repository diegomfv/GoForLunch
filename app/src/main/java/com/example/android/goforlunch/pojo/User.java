package com.example.android.goforlunch.pojo;

import com.example.android.goforlunch.strings.StringValues;

/**
 * Created by Diego Fajardo on 24/05/2018.
 */
public class User {

    String firstName;
    String lastName;
    String email;
    String group;
    //placeId is the restaurant placeId
    String placeId;
    String restaurant;
    String restaurantType;

    public User() {
    }

    public User(String firstName, String lastName, String email, String group, String placeId, String restaurant, String restaurantType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.group = group;
        this.placeId = placeId;
        this.restaurant = restaurant;
        this.restaurantType = restaurantType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public String getRestaurantType() {
        return restaurantType;
    }

    public void setRestaurantType(String restaurantType) {
        this.restaurantType = restaurantType;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", group='" + group + '\'' +
                ", placeId='" + placeId + '\'' +
                ", restaurant='" + restaurant + '\'' +
                ", restaurantType='" + restaurantType + '\'' +
                '}';
    }
}
