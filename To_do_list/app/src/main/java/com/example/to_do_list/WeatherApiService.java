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
    
    // 기상청 중기예보 API 엔드포인트 (육상예보)
    @GET("getMidLandFcst")
    Call<WeatherData.MidWeatherResponse> getMidLandForecast(
        @Query("serviceKey") String serviceKey,
        @Query("pageNo") int pageNo,
        @Query("numOfRows") int numOfRows,
        @Query("dataType") String dataType,
        @Query("regId") String regId,
        @Query("tmFc") String tmFc
    );
    
    // 기상청 중기기온예보 API 엔드포인트
    @GET("getMidTa")
    Call<WeatherData.MidTemperatureResponse> getMidTemperatureForecast(
        @Query("serviceKey") String serviceKey,
        @Query("pageNo") int pageNo,
        @Query("numOfRows") int numOfRows,
        @Query("dataType") String dataType,
        @Query("regId") String regId,
        @Query("tmFc") String tmFc
    );
} 