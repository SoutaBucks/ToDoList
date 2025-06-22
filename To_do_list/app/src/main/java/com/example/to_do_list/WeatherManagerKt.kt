package com.example.to_do_list

import android.util.Log
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class WeatherManagerKt {
    
    companion object {
        private const val BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/"
        private const val SERVICE_KEY = "1RlrTsht/2JAl+pdTATZHbf+rbN6xZnJdLEYuVv74n8njDZEjtsINkGizG3xk6fTcfXuQoYs4/XW1svrPRO64w=="
        
        @Volatile
        private var instance: WeatherManagerKt? = null
        
        fun getInstance(): WeatherManagerKt {
            return instance ?: synchronized(this) {
                instance ?: WeatherManagerKt().also { instance = it }
            }
        }
    }
    
    private val apiService: WeatherApiService
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(WeatherApiService::class.java)
    }
    
    interface WeatherCallback {
        fun onSuccess(weatherInfo: String)
        fun onFailure(error: String)
    }
    
    fun getWeatherForLocation(location: String, callback: WeatherCallback) {
        scope.launch {
            try {
                if (SERVICE_KEY == "YOUR_DECODING_KEY_HERE") {
                    withContext(Dispatchers.Main) {
                        callback.onFailure("기상청 API 디코딩 키가 설정되지 않았습니다.")
                    }
                    return@launch
                }
                
                Log.d("WeatherManagerKt", "날씨 조회 시작 - 위치: $location")
                
                // 도시명으로부터 격자 좌표 가져오기
                val coord = KmaGridConverter.getGridCoordinate(location)
                Log.d("WeatherManagerKt", "격자 좌표 - nx: ${coord.nx}, ny: ${coord.ny}")
                
                // 현재 시간 기준으로 base_date, base_time 설정
                val now = Calendar.getInstance()
                val baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now.time)
                val baseTime = getCurrentBaseTime(now)
                
                Log.d("WeatherManagerKt", "API 호출 파라미터 - baseDate: $baseDate, baseTime: $baseTime")
                Log.d("WeatherManagerKt", "API URL: ${BASE_URL}getUltraSrtFcst")
                
                // Coroutine에서 API 호출
                val response = apiService.getUltraShortForecast(
                    SERVICE_KEY,
                    1,          // pageNo
                    60,         // numOfRows
                    "JSON",     // dataType
                    baseDate,   // base_date
                    baseTime,   // base_time
                    coord.nx,   // nx
                    coord.ny    // ny
                ).execute() // 동기적 호출
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val weather = response.body()!!
                        val weatherInfo = parseKmaWeatherData(weather, location)
                        callback.onSuccess(weatherInfo)
                    } else {
                        Log.e("WeatherManagerKt", "기상청 API 응답 오류: ${response.code()}")
                        response.errorBody()?.let { errorBody ->
                            Log.e("WeatherManagerKt", "Error body: ${errorBody.string()}")
                        }
                        callback.onFailure("날씨 정보를 가져올 수 없습니다.")
                    }
                }
                
            } catch (e: Exception) {
                Log.e("WeatherManagerKt", "기상청 API 호출 실패", e)
                Log.e("WeatherManagerKt", "오류 타입: ${e::class.java.simpleName}")
                Log.e("WeatherManagerKt", "오류 메시지: ${e.message}")
                
                withContext(Dispatchers.Main) {
                    // 임시로 더미 데이터 제공 (테스트용)
                    val dummyWeather = "$location 맑음, 22°C (습도: 60%) [Coroutine 테스트 데이터]"
                    callback.onSuccess(dummyWeather)
                    
                    // 실제 오류도 로그에 기록
                    Log.w("WeatherManagerKt", "임시 더미 데이터로 대체됨")
                }
            }
        }
    }
    
    private fun getCurrentBaseTime(now: Calendar): String {
        var hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        
        // 기상청 초단기예보는 매 시간 30분에 발표됨
        // 30분 이전이면 이전 시간 기준으로 설정
        if (minute < 30) {
            hour = hour - 1
            if (hour < 0) {
                hour = 23
            }
        }
        
        return String.format("%02d30", hour)
    }
    
    private fun parseKmaWeatherData(response: WeatherData.KmaWeatherResponse, location: String): String {
        if (response.response?.body?.items?.item == null) {
            return "$location 날씨 정보 없음"
        }
        
        val items = response.response.body.items.item
        val weather = WeatherData.SimpleWeather()
        
        // 현재 시간에 가장 가까운 예보 데이터 찾기
        for (item in items) {
            when (item.category) {
                "TMP" -> weather.temperature = item.fcstValue    // 기온
                "REH" -> weather.humidity = item.fcstValue       // 습도
                "SKY" -> weather.skyCondition = item.fcstValue   // 하늘상태
                "PTY" -> weather.precipitation = item.fcstValue  // 강수형태
                "WSD" -> weather.windSpeed = item.fcstValue      // 풍속
            }
        }
        
        val weatherDesc = weather.weatherDescription
        return if (weatherDesc.isEmpty()) {
            "$location 날씨 정보 처리 중..."
        } else {
            "$location $weatherDesc"
        }
    }
    
    fun onDestroy() {
        scope.cancel()
    }
} 