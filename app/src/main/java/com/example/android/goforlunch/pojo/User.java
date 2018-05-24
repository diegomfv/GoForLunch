package com.example.android.goforlunch.pojo;

import com.example.android.goforlunch.strings.StringValues;

/**
 * Created by Diego Fajardo on 24/05/2018.
 */
public class User {

    String firstname;
    String lastname;
    String email;
    String group;
    String restaurant;
    String restaurantType;

    public User() {
    }

    public User(String firstname, String lastname, String email, String group, String restaurant, String restaurantType) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.group = group;
        this.restaurant = restaurant;
        this.restaurantType = restaurantType;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
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
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", group='" + group + '\'' +
                ", restaurant='" + restaurant + '\'' +
                ", restaurantType='" + restaurantType + '\'' +
                '}';
    }
}
