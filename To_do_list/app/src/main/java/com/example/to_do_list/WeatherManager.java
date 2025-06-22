package com.example.to_do_list;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeatherManager {
    
    private static final String BASE_URL = "https://apis.data.go.kr/1360000/MidFcstInfoService/";
    private static final String SERVICE_KEY = "1RlrTsht/2JAl+pdTATZHbf+rbN6xZnJdLEYuVv74n8njDZEjtsINkGizG3xk6fTcfXuQoYs4/XW1svrPRO64w==";
    private static WeatherManager instance;
    private WeatherApiService apiService;
    
    public interface WeatherCallback {
        void onSuccess(String weatherInfo);
        void onFailure(String error);
    }
    
    private WeatherManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(WeatherApiService.class);
    }
    
    public static WeatherManager getInstance() {
        if (instance == null) {
            instance = new WeatherManager();
        }
        return instance;
    }
    
    public void getWeatherForLocation(String location, WeatherCallback callback) {
        Log.d("WeatherManager", "중기예보 조회 시작 - 위치: " + location);
        
        // 지역명으로부터 중기예보 지역 코드 가져오기
        String regId = getRegionId(location);
        Log.d("WeatherManager", "중기예보 지역코드: " + regId);
        
        // 현재 시간 기준으로 tmFc 설정 (중기예보는 하루 2회 06:00, 18:00에 발표)
        String tmFc = getCurrentTmFc();
        
        Log.d("WeatherManager", "API 호출 파라미터 - regId: " + regId + ", tmFc: " + tmFc);
        Log.d("WeatherManager", "API URL: " + BASE_URL + "getMidLandFcst");
        
        Call<WeatherData.MidWeatherResponse> call = apiService.getMidLandForecast(
            SERVICE_KEY,
            1,          // pageNo
            10,         // numOfRows
            "JSON",     // dataType
            regId,      // regId
            tmFc        // tmFc
        );
        
        call.enqueue(new Callback<WeatherData.MidWeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherData.MidWeatherResponse> call, Response<WeatherData.MidWeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherData.MidWeatherResponse weather = response.body();
                    String weatherInfo = parseMidWeatherData(weather, location);
                    callback.onSuccess(weatherInfo);
                } else {
                    Log.e("WeatherManager", "중기예보 API 응답 오류: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("WeatherManager", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e("WeatherManager", "Error reading error body", e);
                        }
                    }
                    callback.onFailure("날씨 정보를 가져올 수 없습니다.");
                }
            }
            
            @Override
            public void onFailure(Call<WeatherData.MidWeatherResponse> call, Throwable t) {
                Log.e("WeatherManager", "중기예보 API 호출 실패", t);
                Log.e("WeatherManager", "오류 타입: " + t.getClass().getSimpleName());
                Log.e("WeatherManager", "오류 메시지: " + t.getMessage());
                
                // 이모티콘이 포함된 더미 데이터 제공 (테스트용)
                String dummyWeather = "☀️ 맑음 🌡️ 18°C ~ 25°C [" + location + " 중기예보 테스트 데이터]";
                callback.onSuccess(dummyWeather);
                
                // 실제 오류도 로그에 기록
                Log.w("WeatherManager", "임시 더미 데이터로 대체됨");
            }
        });
    }
    
    /**
     * 중기예보 발표시간 설정 (06:00, 18:00)
     */
    private String getCurrentTmFc() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        
        // 06:00 이전이면 이전 날 18:00 기준
        // 18:00 이전이면 당일 06:00 기준
        // 18:00 이후면 당일 18:00 기준
        if (hour < 6) {
            now.add(Calendar.DAY_OF_MONTH, -1);
            return new SimpleDateFormat("yyyyMMdd1800", Locale.getDefault()).format(now.getTime());
        } else if (hour < 18) {
            return new SimpleDateFormat("yyyyMMdd0600", Locale.getDefault()).format(now.getTime());
        } else {
            return new SimpleDateFormat("yyyyMMdd1800", Locale.getDefault()).format(now.getTime());
        }
    }
    
    /**
     * 지역명을 중기예보 지역코드로 변환
     */
    private String getRegionId(String location) {
        if (location.contains("서울") || location.contains("인천") || location.contains("경기")) {
            return "11B00000";
        } else if (location.contains("강원") || location.contains("춘천")) {
            return "11D10000";
        } else if (location.contains("대전") || location.contains("세종") || location.contains("충남")) {
            return "11C20000";
        } else if (location.contains("충북") || location.contains("청주")) {
            return "11C10000";
        } else if (location.contains("광주") || location.contains("전남")) {
            return "11F20000";
        } else if (location.contains("전북") || location.contains("전주")) {
            return "11F10000";
        } else if (location.contains("대구") || location.contains("경북") || location.contains("안동")) {
            return "11H10000";
        } else if (location.contains("부산") || location.contains("울산") || location.contains("경남")) {
            return "11H20000";
        } else if (location.contains("제주")) {
            return "11G00000";
        } else {
            return "11B00000"; // 기본값: 서울/인천/경기
        }
    }
    
    /**
     * 중기예보 데이터 파싱
     */
    private String parseMidWeatherData(WeatherData.MidWeatherResponse response, String location) {
        if (response.response == null || 
            response.response.body == null || 
            response.response.body.items == null || 
            response.response.body.items.item == null ||
            response.response.body.items.item.isEmpty()) {
            return location + " ❓ 날씨 정보 없음";
        }
        
        WeatherData.MidItem item = response.response.body.items.item.get(0);
        
        // 3일 후 예보 사용 (가장 가까운 예보)
        String weatherCondition = item.wf3Am != null ? item.wf3Am : 
                                 (item.wf3Pm != null ? item.wf3Pm : "정보없음");
        String minTemp = item.taMin3;
        String maxTemp = item.taMax3;
        
        WeatherData.MidWeatherInfo weatherInfo = new WeatherData.MidWeatherInfo(minTemp, maxTemp, weatherCondition);
        String formattedInfo = weatherInfo.getFormattedWeatherInfo();
        
        return location + " " + formattedInfo;
    }
} 