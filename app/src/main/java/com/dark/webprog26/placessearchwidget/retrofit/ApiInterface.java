package com.dark.webprog26.placessearchwidget.retrofit;

import com.dark.webprog26.placessearchwidget.models.PlacesResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by webpr on 27.02.2017.
 */

public interface ApiInterface {

    @GET("maps/api/place/textsearch/json")
    public Call<PlacesResponseModel> getPlaces(@Query("query") String query,
                                               @Query(value = "location", encoded = true) String location,
                                               @Query("key") String apiKey);
}
