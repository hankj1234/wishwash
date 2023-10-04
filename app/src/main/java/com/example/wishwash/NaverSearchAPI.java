package com.example.wishwash;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface NaverSearchAPI {
    @GET("v1/search/local")
    Call<SearchResponse> searchPlace(
            @Header("X-NCP-APIGW-API-KEY-ID") String clientId,
            @Header("X-NCP-APIGW-API-KEY") String clientSecret,
            @Query("query") String query,
            @Query("radius") int radius
    );
}