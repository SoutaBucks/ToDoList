package com.example.to_do_list;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AndroidCalendarManager {
    private static final String TAG = "AndroidCalendarManager";
    private Context context;
    private ContentResolver contentResolver;
    private long calendarId = -1;

    public AndroidCalendarManager(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
        initializeCalendar();
    }

    private void initializeCalendar() {
        // 기본 캘린더 ID 가져오기
        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        };
        
        String selection = CalendarContract.Calendars.VISIBLE + " = 1";
        
        try (Cursor cursor = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                null,
                CalendarContract.Calendars._ID + " ASC")) {
            
            if (cursor != null && cursor.moveToFirst()) {
                // 첫 번째 사용 가능한 캘린더 사용
                calendarId = cursor.getLong(0);
                String calendarName = cursor.getString(1);
                Log.d(TAG, "사용할 캘린더: " + calendarName + " (ID: " + calendarId + ")");
            } else {
                Log.w(TAG, "사용 가능한 캘린더가 없습니다.");
            }
        } catch (Exception e) {
            Log.e(TAG, "캘린더 초기화 실패: " + e.getMessage());
        }
    }

    public boolean isCalendarAvailable() {
        return calendarId != -1;
    }

    public String addTodoToCalendar(Todo todo) {
        if (!isCalendarAvailable()) {
            return "사용 가능한 캘린더가 없습니다.";
        }

        try {
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
            values.put(CalendarContract.Events.TITLE, todo.getTitle());
            values.put(CalendarContract.Events.DESCRIPTION, todo.getDescription());
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            // 마감일이 있는 경우
            if (todo.getDueDate() != null) {
                long startTime = todo.getDueDate().getTime();
                long endTime = startTime + (60 * 60 * 1000); // 1시간 후
                
                values.put(CalendarContract.Events.DTSTART, startTime);
                values.put(CalendarContract.Events.DTEND, endTime);
            } else {
                // 마감일이 없으면 오늘 날짜로 설정
                Calendar calendar = Calendar.getInstance();
                long startTime = calendar.getTimeInMillis();
                long endTime = startTime + (60 * 60 * 1000); // 1시간 후
                
                values.put(CalendarContract.Events.DTSTART, startTime);
                values.put(CalendarContract.Events.DTEND, endTime);
            }

            // 이벤트 추가
            Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);
            
            if (uri != null) {
                long eventId = Long.parseLong(uri.getLastPathSegment());
                todo.setCalendarEventId(String.valueOf(eventId));
                Log.d(TAG, "이벤트 추가 성공: " + eventId);
                return "할일이 캘린더에 추가되었습니다.";
            } else {
                return "이벤트 추가에 실패했습니다.";
            }
            
        } catch (Exception e) {
            Log.e(TAG, "이벤트 추가 실패: " + e.getMessage());
            return "이벤트 추가 실패: " + e.getMessage();
        }
    }

    public String updateTodoInCalendar(Todo todo) {
        if (!isCalendarAvailable()) {
            return "사용 가능한 캘린더가 없습니다.";
        }

        String eventId = todo.getCalendarEventId();
        if (eventId == null || eventId.isEmpty()) {
            // 이벤트 ID가 없으면 새로 추가
            return addTodoToCalendar(todo);
        }

        try {
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.TITLE, todo.getTitle());
            values.put(CalendarContract.Events.DESCRIPTION, todo.getDescription());

            // 마감일이 있는 경우
            if (todo.getDueDate() != null) {
                long startTime = todo.getDueDate().getTime();
                long endTime = startTime + (60 * 60 * 1000); // 1시간 후
                
                values.put(CalendarContract.Events.DTSTART, startTime);
                values.put(CalendarContract.Events.DTEND, endTime);
            }

            // 이벤트 업데이트
            Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(eventId));
            int rows = contentResolver.update(updateUri, values, null, null);
            
            if (rows > 0) {
                Log.d(TAG, "이벤트 업데이트 성공: " + eventId);
                return "캘린더가 업데이트되었습니다.";
            } else {
                // 업데이트 실패 시 새로 추가
                return addTodoToCalendar(todo);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "이벤트 업데이트 실패: " + e.getMessage());
            // 업데이트 실패 시 새로 추가
            return addTodoToCalendar(todo);
        }
    }

    public String removeTodoFromCalendar(Todo todo) {
        if (!isCalendarAvailable()) {
            return "사용 가능한 캘린더가 없습니다.";
        }

        String eventId = todo.getCalendarEventId();
        if (eventId == null || eventId.isEmpty()) {
            return "캘린더 이벤트 ID가 없습니다.";
        }

        try {
            Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(eventId));
            int rows = contentResolver.delete(deleteUri, null, null);
            
            if (rows > 0) {
                Log.d(TAG, "이벤트 삭제 성공: " + eventId);
                return "캘린더에서 삭제되었습니다.";
            } else {
                return "캘린더에서 삭제 실패: 이벤트를 찾을 수 없습니다.";
            }
            
        } catch (Exception e) {
            Log.e(TAG, "이벤트 삭제 실패: " + e.getMessage());
            return "캘린더에서 삭제 실패: " + e.getMessage();
        }
    }

    public String syncAllTodosToCalendar(List<Todo> todos) {
        if (!isCalendarAvailable()) {
            return "사용 가능한 캘린더가 없습니다.";
        }

        int successCount = 0;
        int failCount = 0;

        for (Todo todo : todos) {
            try {
                if (todo.getCalendarEventId() == null || todo.getCalendarEventId().isEmpty()) {
                    // 새로 추가
                    String result = addTodoToCalendar(todo);
                    if (result.contains("추가되었습니다")) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } else {
                    // 업데이트
                    String result = updateTodoInCalendar(todo);
                    if (result.contains("업데이트되었습니다") || result.contains("추가되었습니다")) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Todo 동기화 실패: " + todo.getTitle() + " - " + e.getMessage());
                failCount++;
            }
        }

        return String.format("동기화 완료: 성공 %d개, 실패 %d개", successCount, failCount);
    }

    public List<String> getCalendarEvents() {
        List<String> events = new ArrayList<>();
        
        if (!isCalendarAvailable()) {
            return events;
        }

        try {
            String[] projection = new String[]{
                    CalendarContract.Events._ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DESCRIPTION,
                    CalendarContract.Events.DTSTART
            };
            
            String selection = CalendarContract.Events.CALENDAR_ID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(calendarId)};
            
            try (Cursor cursor = contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    CalendarContract.Events.DTSTART + " ASC")) {
                
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
                        String description = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
                        long startTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART));
                        
                        Date date = new Date(startTime);
                        String eventInfo = String.format("%s\n(%s)", title, date.toString());
                        events.add(eventInfo);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "이벤트 목록 가져오기 실패: " + e.getMessage());
        }

        return events;
    }

    public String getCalendarInfo() {
        if (!isCalendarAvailable()) {
            return "사용 가능한 캘린더가 없습니다.";
        }
        
        List<String> events = getCalendarEvents();
        return events.size() + "개의 이벤트가 있습니다.";
    }
} 