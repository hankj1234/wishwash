package com.example.wishwash;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface NaverSearchAPI {
    @GET("https://openapi.naver.com/v1/search/local")
    Call<SearchResponse> searchPlace(@Header("X-NCP-APIGW-API-KEY-ID:{pppo35lowg}") String clientId,
                                     @Header("X-NCP-APIGW-API-KEY:{Fjc09cPrMhjKQlQLzmByyd9G46O1WTfL7g18IEZx}") String clientSecret,
                                     @Query("query") String query);
}