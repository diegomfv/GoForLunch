package com.example.android.goforlunch;

/**
 * Created by Diego Fajardo on 29/05/2018.
 */
public class TestObjectCoworker {

    private String name;
    private String group;
    private String restaurant;

    private TestObjectCoworker(final Builder builder) {
        name = builder.name;
        group = builder.group;
        restaurant = builder.restaurant;
    }

    static class Builder {

        private String name;
        private String group;
        private String restaurant;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setRestaurant(String restaurant) {
            this.restaurant = restaurant;
            return this;
        }

        public TestObjectCoworker create() {
            return new TestObjectCoworker(this);
        }


    }
}
