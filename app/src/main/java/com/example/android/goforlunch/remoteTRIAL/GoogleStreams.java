package com.example.android.goforlunch.remoteTRIAL;

import android.content.Context;

import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.data.RestaurantEntry;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.models.modelnearby.PlaceByNearby;
import com.example.android.goforlunch.remote.RetrofitClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Diego Fajardo on 18/06/2018.
 */
public class GoogleStreams {

    public static Observable<List<RestaurantEntry>> getDataFromDatabase (Context context) {
        return AppDatabase.getInstance(context).restaurantDao().getAllRestaurantsNotLiveData().


    }

    public static Observable<PlaceByNearby> streamFetchNearbyPlaces (LatLngForRetrofit myPosition, String rankBy, String type, String key) {
        GoogleService googleService = RetrofitClient.getNearbyClient().create(GoogleService.class);
        return googleService.fetchDataNearby(myPosition, rankBy, type, key)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .timeout(10, TimeUnit.SECONDS);

    }




    public static Observable<List<GithubUser>> streamFetchUserFollowing (String username) {
        GithubService githubService = GithubService.retrofit.create(GithubService.class);
        return githubService.getFollowing(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);

    }

    // 1 - Create a stream that will get user infos on Github API
    public static Observable<GithubUserInfo> streamFetchUserInfos(String username){
        GithubService gitHubService = GithubService.retrofit.create(GithubService.class);
        return gitHubService.getUserInfos(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    // 2 - Create a stream that will :
    //     A. Fetch all users followed by "username"
    //     B. Return the first user of the list
    //     C. Fetch details of the first user
    public static Observable<GithubUserInfo> streamFetchUserFollowingAndFetchFirstUserInfos(String username){
        return streamFetchUserFollowing(username) // A.
                .map(new Function<List<GithubUser>, GithubUser>() {
                    @Override
                    public GithubUser apply(List<GithubUser> users) throws Exception {
                        return users.get(0); // B.
                    }
                })
                .flatMap(new Function<GithubUser, Observable<GithubUserInfo>>() {
                    @Override
                    public Observable<GithubUserInfo> apply(GithubUser user) throws Exception {
                        // C.
                        return streamFetchUserInfos(user.getLogin());
                    }
                });
    }





}
