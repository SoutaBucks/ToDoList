package com.example.to_do_list;

public class HangulUtils {

    private static final char[] CHO =
            {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

    public static String getChosung(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch >= 0xAC00 && ch <= 0xD7A3) { // 한글 범위
                int uniVal = ch - 0xAC00;
                int choIndex = uniVal / (21 * 28);
                sb.append(CHO[choIndex]);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static boolean isHangul(char ch) {
        return ch >= 0xAC00 && ch <= 0xD7A3;
    }
} 