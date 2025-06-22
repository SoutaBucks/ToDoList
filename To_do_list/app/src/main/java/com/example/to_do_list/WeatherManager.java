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
    
    private static final String BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/";
    private static final String SERVICE_KEY = "1RlrTsht/2JAl+pdTATZHbf+rbN6xZnJdLEYuVv74n8njDZEjtsINkGizG3xk6fTcfXuQoYs4/XW1svrPRO64w=="; // 기상청 API 디코딩 키로 교체 필요
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
        if (SERVICE_KEY.equals("YOUR_DECODING_KEY_HERE")) {
            callback.onFailure("기상청 API 디코딩 키가 설정되지 않았습니다.");
            return;
        }
        
        Log.d("WeatherManager", "날씨 조회 시작 - 위치: " + location);
        
        // 도시명으로부터 격자 좌표 가져오기
        KmaGridConverter.GridCoordinate coord = KmaGridConverter.getGridCoordinate(location);
        Log.d("WeatherManager", "격자 좌표 - nx: " + coord.nx + ", ny: " + coord.ny);
        
        // 현재 시간 기준으로 base_date, base_time 설정
        Calendar now = Calendar.getInstance();
        String baseDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now.getTime());
        String baseTime = getCurrentBaseTime(now);
        
        Log.d("WeatherManager", "API 호출 파라미터 - baseDate: " + baseDate + ", baseTime: " + baseTime);
        Log.d("WeatherManager", "API URL: " + BASE_URL + "getUltraSrtFcst");
        
        Call<WeatherData.KmaWeatherResponse> call = apiService.getUltraShortForecast(
            SERVICE_KEY,
            1,          // pageNo
            60,         // numOfRows (충분한 데이터 확보)
            "JSON",     // dataType
            baseDate,   // base_date
            baseTime,   // base_time
            coord.nx,   // nx
            coord.ny    // ny
        );
        
        call.enqueue(new Callback<WeatherData.KmaWeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherData.KmaWeatherResponse> call, Response<WeatherData.KmaWeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherData.KmaWeatherResponse weather = response.body();
                    String weatherInfo = parseKmaWeatherData(weather, location);
                    callback.onSuccess(weatherInfo);
                } else {
                    Log.e("WeatherManager", "기상청 API 응답 오류: " + response.code());
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
            public void onFailure(Call<WeatherData.KmaWeatherResponse> call, Throwable t) {
                Log.e("WeatherManager", "기상청 API 호출 실패", t);
                Log.e("WeatherManager", "오류 타입: " + t.getClass().getSimpleName());
                Log.e("WeatherManager", "오류 메시지: " + t.getMessage());
                
                // 임시로 더미 데이터 제공 (테스트용)
                String dummyWeather = location + " 맑음, 22°C (습도: 60%) [테스트 데이터]";
                callback.onSuccess(dummyWeather);
                
                // 실제 오류도 로그에 기록
                Log.w("WeatherManager", "임시 더미 데이터로 대체됨");
            }
        });
    }
    
    private String getCurrentBaseTime(Calendar now) {
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        
        // 기상청 초단기예보는 매 시간 30분에 발표됨
        // 30분 이전이면 이전 시간 기준으로 설정
        if (minute < 30) {
            hour = hour - 1;
            if (hour < 0) {
                hour = 23;
            }
        }
        
        return String.format("%02d30", hour);
    }
    
    private String parseKmaWeatherData(WeatherData.KmaWeatherResponse response, String location) {
        if (response.response == null || 
            response.response.body == null || 
            response.response.body.items == null || 
            response.response.body.items.item == null) {
            return location + " 날씨 정보 없음";
        }
        
        List<WeatherData.Item> items = response.response.body.items.item;
        WeatherData.SimpleWeather weather = new WeatherData.SimpleWeather();
        
        // 현재 시간에 가장 가까운 예보 데이터 찾기
        for (WeatherData.Item item : items) {
            switch (item.category) {
                case "TMP":  // 기온
                    weather.temperature = item.fcstValue;
                    break;
                case "REH":  // 습도
                    weather.humidity = item.fcstValue;
                    break;
                case "SKY":  // 하늘상태
                    weather.skyCondition = item.fcstValue;
                    break;
                case "PTY":  // 강수형태
                    weather.precipitation = item.fcstValue;
                    break;
                case "WSD":  // 풍속
                    weather.windSpeed = item.fcstValue;
                    break;
            }
        }
        
        String weatherDesc = weather.getWeatherDescription();
        if (weatherDesc.isEmpty()) {
            return location + " 날씨 정보 처리 중...";
        }
        
        return location + " " + weatherDesc;
    }
} 