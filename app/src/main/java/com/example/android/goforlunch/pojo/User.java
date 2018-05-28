package com.example.android.goforlunch.pojo;

/**
 * Created by Diego Fajardo on 24/05/2018.
 */
public class User {

    String firstName;
    String lastName;
    String email;
    String group;
    //placeId is the restaurantName placeId
    String placeId;
    String restaurantName;
    String restaurantType;
    String imageUrl;
    String address;
    String rating;
    String phone;
    String websiteUrl;


    public User() {
    }

    public User(String firstName, String lastName, String email, String group, String placeId, String restaurantName, String restaurantType, String imageUrl, String address, String rating, String phone, String websiteUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.group = group;
        this.placeId = placeId;
        this.restaurantName = restaurantName;
        this.restaurantType = restaurantType;
        this.imageUrl = imageUrl;
        this.address = address;
        this.rating = rating;
        this.phone = phone;
        this.websiteUrl = websiteUrl;
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

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantType() {
        return restaurantType;
    }

    public void setRestaurantType(String restaurantType) {
        this.restaurantType = restaurantType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", group='" + group + '\'' +
                ", placeId='" + placeId + '\'' +
                ", restaurantName='" + restaurantName + '\'' +
                ", restaurantType='" + restaurantType + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", address='" + address + '\'' +
                ", rating='" + rating + '\'' +
                ", phone='" + phone + '\'' +
                ", websiteUrl='" + websiteUrl + '\'' +
                '}';
    }
}
