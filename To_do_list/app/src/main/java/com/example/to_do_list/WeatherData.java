package com.example.to_do_list;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherData {
    
    // 기상청 단기예보 API 응답 구조
    public static class KmaWeatherResponse {
        @SerializedName("response")
        public Response response;
    }
    
    public static class Response {
        @SerializedName("header")
        public Header header;
        
        @SerializedName("body")
        public Body body;
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
    
    public static class Items {
        @SerializedName("item")
        public List<Item> item;
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
} 