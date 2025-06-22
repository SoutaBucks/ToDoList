package com.example.to_do_list;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    
    // 기상청 단기예보 API 엔드포인트
    @GET("getUltraSrtFcst")
    Call<WeatherData.KmaWeatherResponse> getUltraShortForecast(
        @Query("serviceKey") String serviceKey,
        @Query("pageNo") int pageNo,
        @Query("numOfRows") int numOfRows,
        @Query("dataType") String dataType,
        @Query("base_date") String baseDate,
        @Query("base_time") String baseTime,
        @Query("nx") int nx,
        @Query("ny") int ny
    );
} 