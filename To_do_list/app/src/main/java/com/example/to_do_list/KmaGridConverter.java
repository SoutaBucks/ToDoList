package com.example.to_do_list;

import java.util.HashMap;
import java.util.Map;

public class KmaGridConverter {
    
    // 도시별 격자 좌표 (nx, ny)
    private static final Map<String, GridCoordinate> CITY_COORDINATES = new HashMap<>();
    
    static {
        // 주요 도시들의 격자 좌표 (기상청 공식 좌표)
        CITY_COORDINATES.put("서울", new GridCoordinate(60, 127));
        CITY_COORDINATES.put("부산", new GridCoordinate(98, 76));
        CITY_COORDINATES.put("대구", new GridCoordinate(89, 90));
        CITY_COORDINATES.put("인천", new GridCoordinate(55, 124));
        CITY_COORDINATES.put("광주", new GridCoordinate(58, 74));
        CITY_COORDINATES.put("대전", new GridCoordinate(67, 100));
        CITY_COORDINATES.put("울산", new GridCoordinate(102, 84));
        CITY_COORDINATES.put("수원", new GridCoordinate(60, 121));
        CITY_COORDINATES.put("고양", new GridCoordinate(57, 128));
        CITY_COORDINATES.put("용인", new GridCoordinate(64, 119));
        CITY_COORDINATES.put("성남", new GridCoordinate(63, 124));
        CITY_COORDINATES.put("청주", new GridCoordinate(69, 106));
        CITY_COORDINATES.put("안산", new GridCoordinate(58, 121));
        CITY_COORDINATES.put("안양", new GridCoordinate(59, 123));
        CITY_COORDINATES.put("전주", new GridCoordinate(63, 89));
        CITY_COORDINATES.put("천안", new GridCoordinate(65, 114));
        CITY_COORDINATES.put("남양주", new GridCoordinate(64, 128));
        CITY_COORDINATES.put("화성", new GridCoordinate(57, 119));
        CITY_COORDINATES.put("평택", new GridCoordinate(62, 114));
        CITY_COORDINATES.put("의정부", new GridCoordinate(61, 130));
        CITY_COORDINATES.put("시흥", new GridCoordinate(57, 123));
        CITY_COORDINATES.put("파주", new GridCoordinate(56, 131));
        CITY_COORDINATES.put("김해", new GridCoordinate(95, 77));
        CITY_COORDINATES.put("춘천", new GridCoordinate(73, 134));
        CITY_COORDINATES.put("포항", new GridCoordinate(102, 94));
        CITY_COORDINATES.put("진주", new GridCoordinate(90, 75));
        CITY_COORDINATES.put("창원", new GridCoordinate(90, 77));
        CITY_COORDINATES.put("원주", new GridCoordinate(76, 122));
        CITY_COORDINATES.put("목포", new GridCoordinate(50, 67));
        CITY_COORDINATES.put("제주", new GridCoordinate(52, 38));
        CITY_COORDINATES.put("서귀포", new GridCoordinate(52, 33));
        CITY_COORDINATES.put("강릉", new GridCoordinate(92, 131));
        CITY_COORDINATES.put("속초", new GridCoordinate(87, 141));
        CITY_COORDINATES.put("동해", new GridCoordinate(97, 127));
        CITY_COORDINATES.put("삼척", new GridCoordinate(98, 125));
        CITY_COORDINATES.put("태백", new GridCoordinate(95, 119));
        CITY_COORDINATES.put("정선", new GridCoordinate(89, 123));
        CITY_COORDINATES.put("영월", new GridCoordinate(86, 119));
        CITY_COORDINATES.put("평창", new GridCoordinate(84, 123));
        CITY_COORDINATES.put("홍천", new GridCoordinate(75, 133));
        CITY_COORDINATES.put("횡성", new GridCoordinate(77, 125));
        CITY_COORDINATES.put("철원", new GridCoordinate(65, 139));
        CITY_COORDINATES.put("화천", new GridCoordinate(72, 139));
        CITY_COORDINATES.put("양구", new GridCoordinate(77, 139));
        CITY_COORDINATES.put("인제", new GridCoordinate(80, 138));
        CITY_COORDINATES.put("고성", new GridCoordinate(85, 145));
    }
    
    public static class GridCoordinate {
        public final int nx;
        public final int ny;
        
        public GridCoordinate(int nx, int ny) {
            this.nx = nx;
            this.ny = ny;
        }
    }
    
    /**
     * 도시명으로부터 격자 좌표를 반환
     * @param cityName 도시명
     * @return 격자 좌표 (없으면 서울 좌표 반환)
     */
    public static GridCoordinate getGridCoordinate(String cityName) {
        GridCoordinate coord = CITY_COORDINATES.get(cityName);
        if (coord == null) {
            // 기본값으로 서울 좌표 반환
            coord = CITY_COORDINATES.get("서울");
        }
        return coord;
    }
    
    /**
     * 지원하는 도시인지 확인
     * @param cityName 도시명
     * @return 지원 여부
     */
    public static boolean isSupportedCity(String cityName) {
        return CITY_COORDINATES.containsKey(cityName);
    }
} 