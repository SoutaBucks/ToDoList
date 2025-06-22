package com.example.to_do_list;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherData {
    
    // 기상청 단기예보 API 응답 구조
    public static class KmaWeatherResponse {
        @SerializedName("response")
        public Response response;
    }
    
    // 기상청 중기예보 API 응답 구조 (육상예보)
    public static class MidWeatherResponse {
        @SerializedName("response")
        public MidResponse response;
    }
    
    // 기상청 중기기온예보 API 응답 구조
    public static class MidTemperatureResponse {
        @SerializedName("response")
        public MidResponse response;
    }
    
    public static class Response {
        @SerializedName("header")
        public Header header;
        
        @SerializedName("body")
        public Body body;
    }
    
    public static class MidResponse {
        @SerializedName("header")
        public Header header;
        
        @SerializedName("body")
        public MidBody body;
    }
    
    public static class Header {
        @SerializedName("resultCode")
        public String resultCode;
        
        @SerializedName("resultMsg")
        public String resultMsg;
    }
    
    public static class Body {
        @SerializedName("items")
        public Items items;
        
        @SerializedName("pageNo")
        public int pageNo;
        
        @SerializedName("numOfRows")
        public int numOfRows;
        
        @SerializedName("totalCount")
        public int totalCount;
    }
    
    public static class MidBody {
        @SerializedName("items")
        public MidItems items;
        
        @SerializedName("pageNo")
        public int pageNo;
        
        @SerializedName("numOfRows")
        public int numOfRows;
        
        @SerializedName("totalCount")
        public int totalCount;
    }
    
    public static class Items {
        @SerializedName("item")
        public List<Item> item;
    }
    
    public static class MidItems {
        @SerializedName("item")
        public List<MidItem> item;
    }
    
    public static class Item {
        @SerializedName("baseDate")
        public String baseDate;
        
        @SerializedName("baseTime")
        public String baseTime;
        
        @SerializedName("category")
        public String category;
        
        @SerializedName("fcstDate")
        public String fcstDate;
        
        @SerializedName("fcstTime")
        public String fcstTime;
        
        @SerializedName("fcstValue")
        public String fcstValue;
        
        @SerializedName("nx")
        public int nx;
        
        @SerializedName("ny")
        public int ny;
    }
    
    // 중기예보 아이템 구조
    public static class MidItem {
        @SerializedName("regId")
        public String regId;
        
        @SerializedName("wf3Am")
        public String wf3Am;
        
        @SerializedName("wf3Pm")
        public String wf3Pm;
        
        @SerializedName("wf4Am")
        public String wf4Am;
        
        @SerializedName("wf4Pm")
        public String wf4Pm;
        
        @SerializedName("wf5Am")
        public String wf5Am;
        
        @SerializedName("wf5Pm")
        public String wf5Pm;
        
        @SerializedName("wf6Am")
        public String wf6Am;
        
        @SerializedName("wf6Pm")
        public String wf6Pm;
        
        @SerializedName("wf7Am")
        public String wf7Am;
        
        @SerializedName("wf7Pm")
        public String wf7Pm;
        
        @SerializedName("taMin3")
        public String taMin3;
        
        @SerializedName("taMax3")
        public String taMax3;
        
        @SerializedName("taMin4")
        public String taMin4;
        
        @SerializedName("taMax4")
        public String taMax4;
        
        @SerializedName("taMin5")
        public String taMin5;
        
        @SerializedName("taMax5")
        public String taMax5;
        
        @SerializedName("taMin6")
        public String taMin6;
        
        @SerializedName("taMax6")
        public String taMax6;
        
        @SerializedName("taMin7")
        public String taMin7;
        
        @SerializedName("taMax7")
        public String taMax7;
    }
    
    // 간단한 날씨 정보 클래스
    public static class SimpleWeather {
        public String temperature;    // 기온 (TMP)
        public String humidity;       // 습도 (REH)
        public String skyCondition;   // 하늘상태 (SKY)
        public String precipitation;  // 강수형태 (PTY)
        public String windSpeed;      // 풍속 (WSD)
        
        public SimpleWeather() {}
        
        public String getWeatherDescription() {
            StringBuilder desc = new StringBuilder();
            
            // 강수형태 확인
            if (precipitation != null) {
                switch (precipitation) {
                    case "1": desc.append("비, "); break;
                    case "2": desc.append("비/눈, "); break;
                    case "3": desc.append("눈, "); break;
                    case "4": desc.append("소나기, "); break;
                    default: break;
                }
            }
            
            // 하늘상태 확인
            if (skyCondition != null) {
                switch (skyCondition) {
                    case "1": desc.append("맑음"); break;
                    case "3": desc.append("구름많음"); break;
                    case "4": desc.append("흐림"); break;
                    default: desc.append("정보없음"); break;
                }
            }
            
            // 기온 추가
            if (temperature != null) {
                desc.append(", ").append(temperature).append("°C");
            }
            
            // 습도 추가
            if (humidity != null) {
                desc.append(" (습도: ").append(humidity).append("%)");
            }
            
            return desc.toString();
        }
    }
    
    // 중기예보 날씨 정보 클래스 (이모티콘 포함)
    public static class MidWeatherInfo {
        public String minTemp;
        public String maxTemp;
        public String weatherCondition;
        
        public MidWeatherInfo(String minTemp, String maxTemp, String weatherCondition) {
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.weatherCondition = weatherCondition;
        }
        
        /**
         * 날씨 상태에 따른 이모티콘 반환
         */
        public static String getWeatherEmoji(String weather) {
            if (weather == null) return "❓";
            
            weather = weather.toLowerCase();
            
            if (weather.contains("맑음")) {
                return "☀️";
            } else if (weather.contains("구름많음") || weather.contains("구름 많음")) {
                return "⛅";
            } else if (weather.contains("흐림")) {
                return "☁️";
            } else if (weather.contains("비")) {
                return "🌧️";
            } else if (weather.contains("눈")) {
                return "❄️";
            } else if (weather.contains("소나기")) {
                return "🌦️";
            } else if (weather.contains("천둥")) {
                return "⛈️";
            } else if (weather.contains("안개")) {
                return "🌫️";
            } else {
                return "🌤️";
            }
        }
        
        /**
         * 기온에 따른 이모티콘 반환
         */
        public static String getTemperatureEmoji(int temperature) {
            if (temperature >= 30) {
                return "🔥"; // 매우 더움
            } else if (temperature >= 25) {
                return "🌡️"; // 더움
            } else if (temperature >= 15) {
                return "🌤️"; // 따뜻함
            } else if (temperature >= 5) {
                return "🍂"; // 쌀쌀함
            } else if (temperature >= 0) {
                return "🧊"; // 추움
            } else {
                return "❄️"; // 매우 추움
            }
        }
        
        /**
         * 포맷된 날씨 정보 문자열 반환 (이모티콘 포함)
         */
        public String getFormattedWeatherInfo() {
            android.util.Log.d("MidWeatherInfo", "포맷팅 시작 - minTemp: " + minTemp + ", maxTemp: " + maxTemp + ", condition: " + weatherCondition);
            
            StringBuilder info = new StringBuilder();
            
            // 날씨 이모티콘 추가
            String weatherEmoji = getWeatherEmoji(weatherCondition);
            info.append(weatherEmoji).append(" ");
            android.util.Log.d("MidWeatherInfo", "날씨 이모티콘: " + weatherEmoji);
            
            // 날씨 상태
            if (weatherCondition != null && !weatherCondition.isEmpty()) {
                info.append(weatherCondition);
            } else {
                info.append("날씨정보없음");
            }
            
            // 기온 정보
            if (minTemp != null && maxTemp != null && !minTemp.isEmpty() && !maxTemp.isEmpty()) {
                try {
                    int avgTemp = (Integer.parseInt(minTemp) + Integer.parseInt(maxTemp)) / 2;
                    String tempEmoji = getTemperatureEmoji(avgTemp);
                    info.append(" ").append(tempEmoji);
                    info.append(" ").append(minTemp).append("°C ~ ").append(maxTemp).append("°C");
                    android.util.Log.d("MidWeatherInfo", "기온 정보 추가됨: " + minTemp + "°C ~ " + maxTemp + "°C");
                } catch (NumberFormatException e) {
                    info.append(" (기온정보 파싱 실패: " + e.getMessage() + ")");
                    android.util.Log.e("MidWeatherInfo", "기온 파싱 실패", e);
                }
            } else {
                info.append(" (기온정보없음 - minTemp: " + minTemp + ", maxTemp: " + maxTemp + ")");
                android.util.Log.w("MidWeatherInfo", "기온 정보 없음");
            }
            
            String result = info.toString();
            android.util.Log.d("MidWeatherInfo", "최종 결과: " + result);
            return result;
        }
    }
} 