package com.example.to_do_list

import android.util.Log
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class WeatherManagerKt {
    
    companion object {
        private const val BASE_URL = "https://apis.data.go.kr/1360000/MidFcstInfoService/"
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
                Log.d("WeatherManagerKt", "중기예보 조회 시작 - 위치: $location")
                
                // 테스트를 위해 강제로 예외 발생
                throw Exception("테스트를 위한 강제 예외")
                
                // 지역명으로부터 중기예보 지역 코드 가져오기
                val regId = getRegionId(location)
                Log.d("WeatherManagerKt", "중기예보 지역코드: $regId")
                
                // 현재 시간 기준으로 tmFc 설정 (중기예보는 하루 2회 06:00, 18:00에 발표)
                val tmFc = getCurrentTmFc()
                
                Log.d("WeatherManagerKt", "API 호출 파라미터 - regId: $regId, tmFc: $tmFc")
                Log.d("WeatherManagerKt", "API URL: ${BASE_URL}getMidLandFcst")
                
                // 중기예보 API 호출
                val response = apiService.getMidLandForecast(
                    SERVICE_KEY,
                    1,          // pageNo
                    10,         // numOfRows
                    "JSON",     // dataType
                    regId,      // regId
                    tmFc        // tmFc
                ).execute() // 동기적 호출
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val weather = response.body()!!
                        val weatherInfo = parseMidWeatherData(weather, location)
                        callback.onSuccess(weatherInfo)
                    } else {
                        Log.e("WeatherManagerKt", "중기예보 API 응답 오류: ${response.code()}")
                        response.errorBody()?.let { errorBody ->
                            Log.e("WeatherManagerKt", "Error body: ${errorBody.string()}")
                        }
                        callback.onFailure("날씨 정보를 가져올 수 없습니다.")
                    }
                }
                
            } catch (e: Exception) {
                Log.e("WeatherManagerKt", "중기예보 API 호출 실패", e)
                Log.e("WeatherManagerKt", "오류 타입: ${e::class.java.simpleName}")
                Log.e("WeatherManagerKt", "오류 메시지: ${e.message}")
                
                withContext(Dispatchers.Main) {
                    // 이모티콘이 포함된 더미 데이터 제공 (테스트용)
                    val dummyWeather = "☀️ 맑음 🌡️ 18°C ~ 25°C [$location 중기예보 테스트 데이터]"
                    callback.onSuccess(dummyWeather)
                    
                    // 실제 오류도 로그에 기록
                    Log.w("WeatherManagerKt", "임시 더미 데이터로 대체됨")
                }
            }
        }
    }
    
    /**
     * 중기예보 발표시간 설정 (06:00, 18:00)
     */
    private fun getCurrentTmFc(): String {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        
        // 06:00 이전이면 이전 날 18:00 기준
        // 18:00 이전이면 당일 06:00 기준
        // 18:00 이후면 당일 18:00 기준
        if (hour < 6) {
            now.add(Calendar.DAY_OF_MONTH, -1)
            return SimpleDateFormat("yyyyMMdd1800", Locale.getDefault()).format(now.time)
        } else if (hour < 18) {
            return SimpleDateFormat("yyyyMMdd0600", Locale.getDefault()).format(now.time)
        } else {
            return SimpleDateFormat("yyyyMMdd1800", Locale.getDefault()).format(now.time)
        }
    }
    
    /**
     * 지역명을 중기예보 지역코드로 변환
     */
    private fun getRegionId(location: String): String {
        return when {
            location.contains("서울") -> "11B00000"
            location.contains("인천") -> "11B00000"
            location.contains("경기") -> "11B00000"
            location.contains("강원") -> "11D10000"
            location.contains("춘천") -> "11D10000"
            location.contains("대전") -> "11C20000"
            location.contains("세종") -> "11C20000"
            location.contains("충남") -> "11C20000"
            location.contains("충북") -> "11C10000"
            location.contains("청주") -> "11C10000"
            location.contains("광주") -> "11F20000"
            location.contains("전남") -> "11F20000"
            location.contains("전북") -> "11F10000"
            location.contains("전주") -> "11F10000"
            location.contains("대구") -> "11H10000"
            location.contains("경북") -> "11H10000"
            location.contains("안동") -> "11H10000"
            location.contains("부산") -> "11H20000"
            location.contains("울산") -> "11H20000"
            location.contains("경남") -> "11H20000"
            location.contains("제주") -> "11G00000"
            else -> "11B00000" // 기본값: 서울/인천/경기
        }
    }
    
    /**
     * 중기예보 데이터 파싱
     */
    private fun parseMidWeatherData(response: WeatherData.MidWeatherResponse, location: String): String {
        Log.d("WeatherManagerKt", "파싱 시작 - response: $response")
        
        if (response.response?.body?.items?.item == null || response.response.body.items.item.isEmpty()) {
            Log.w("WeatherManagerKt", "응답 데이터가 비어있습니다")
            return "$location ❓ 날씨 정보 없음"
        }
        
        val item = response.response.body.items.item[0]
        Log.d("WeatherManagerKt", "첫 번째 아이템: $item")
        
        // 3일 후 예보 사용 (가장 가까운 예보)
        val weatherCondition = item.wf3Am ?: item.wf3Pm ?: "정보없음"
        val minTemp = item.taMin3
        val maxTemp = item.taMax3
        
        Log.d("WeatherManagerKt", "파싱된 데이터 - weatherCondition: $weatherCondition, minTemp: $minTemp, maxTemp: $maxTemp")
        
        val weatherInfo = WeatherData.MidWeatherInfo(minTemp, maxTemp, weatherCondition)
        val formattedInfo = weatherInfo.getFormattedWeatherInfo()
        
        val result = "$location $formattedInfo"
        Log.d("WeatherManagerKt", "최종 파싱 결과: $result")
        
        return result
    }
    
    fun onDestroy() {
        scope.cancel()
    }
} 