package com.example.android.goforlunch.remote.requesters;

import android.util.Log;

import com.example.android.goforlunch.data.AppDatabase;
import com.example.android.goforlunch.models.modeldistance.Distance;
import com.example.android.goforlunch.models.modeldistance.Elements;
import com.example.android.goforlunch.models.modeldistance.MatrixDistance;
import com.example.android.goforlunch.models.modeldistance.Rows;
import com.example.android.goforlunch.models.modelnearby.LatLngForRetrofit;
import com.example.android.goforlunch.remote.Common;
import com.example.android.goforlunch.remote.GooglePlaceWebAPIService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego Fajardo on 21/05/2018.
 */

/** Class that allows doing requests to Google Distance Matrix API. It allows to get
 * the distance between two places. In our case, one will be a restaurant and the
 * other one will be a position of the user
 * */
public class RequesterDistance {

    private static final String TAG = "RequesterDistance";

    private static final String distanceKey = "AIzaSyCebroRUS4VPwvDky6QXHoNfEr0bPHKkYc";
    private LatLngForRetrofit myPosition;
    private AppDatabase mDb;

    public RequesterDistance(AppDatabase mDb, LatLngForRetrofit myPosition) {
        this.mDb = mDb;
        this.myPosition = myPosition;
    }

    public void doApiRequest(final String placeId) {

        GooglePlaceWebAPIService clientMatrix = Common.getGoogleDistanceMatrixApiService();
        Call<MatrixDistance> callMatrix = clientMatrix.fetchDistance("imperial", "place_id:" + placeId, myPosition, distanceKey);
        callMatrix.enqueue(new Callback<MatrixDistance>() {
            @Override
            public void onResponse(Call<MatrixDistance> call, Response<MatrixDistance> response) {

                Log.d(TAG, "onResponse: correct call");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());

                MatrixDistance matrixDistance = response.body();

                String distanceValue = "nA";

                if (matrixDistance.getRows() != null) {

                    Rows[] rows = matrixDistance.getRows();

                    if (rows[0].getElements() != null) {

                        Elements[] elements = rows[0].getElements();

                        if (elements[0].getDistance() != null) {

                            Distance distance = elements[0].getDistance();
                            distanceValue = distance.getText();

                        }
                    }
                }

                /** Updating the database
                 * */
                mDb.restaurantDao().updateRestaurantDistance(placeId, distanceValue);

            }

            @Override
            public void onFailure(Call<MatrixDistance> call, Throwable t) {

                Log.d(TAG, "onFailure: there was an error");
                Log.d(TAG, "onResponse: url = " + call.request().url().toString());

            }
        });
    }
}
