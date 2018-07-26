package com.example.android.goforlunch.models.pojo;

/**
 * Created by Diego Fajardo on 01/06/2018.
 */
public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String group;
    //placeId is the restaurantName placeId
    private String placeId;
    private String restaurantName;
    private int restaurantType;
    private String imageUrl;
    private String address;
    private String rating;
    private String phone;
    private String websiteUrl;

    private User (final Builder builder) {
        firstName = builder.firstName;
        lastName = builder.lastName;
        email = builder.email;
        group = builder.group;
        placeId = builder.placeId;
        restaurantName = builder.restaurantName;
        restaurantType = builder.restaurantType;
        imageUrl = builder.imageUrl;
        address = builder.address;
        rating = builder.rating;
        phone = builder.phone;
        websiteUrl = builder.websiteUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getGroup() {
        return group;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public int getRestaurantType() {
        return restaurantType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAddress() {
        return address;
    }

    public String getRating() {
        return rating;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public static class Builder {

        private String firstName;
        private String lastName;
        private String email;
        private String group;
        private String placeId;
        private String restaurantName;
        private int restaurantType;
        private String imageUrl;
        private String address;
        private String rating;
        private String phone;
        private String websiteUrl;

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setPlaceId(String placeId) {
            this.placeId = placeId;
            return this;
        }

        public Builder setRestaurantName(String restaurantName) {
            this.restaurantName = restaurantName;
            return this;
        }

        public Builder setRestaurantType(int restaurantType) {
            this.restaurantType = restaurantType;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder setRating(String rating) {
            this.rating = rating;
            return this;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder setWebsiteUrl(String websiteUrl) {
            this.websiteUrl = websiteUrl;
            return this;
        }

        public User create() {
            return new User(this);

        }
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