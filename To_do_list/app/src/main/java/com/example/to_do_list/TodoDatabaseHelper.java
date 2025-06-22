package com.example.to_do_list;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TodoDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "TodoDatabaseHelper";

    // 테이블 이름
    private static final String TABLE_TODOS = "todos";

    // 컬럼 이름들
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_IS_COMPLETED = "is_completed";
    private static final String COLUMN_CREATED_DATE = "created_date";
    private static final String COLUMN_DUE_DATE = "due_date";
    private static final String COLUMN_CALENDAR_EVENT_ID = "calendar_event_id";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_CATEGORY = "category";

    // 테이블 생성 SQL
    private static final String CREATE_TABLE_TODOS = 
        "CREATE TABLE " + TABLE_TODOS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_TITLE + " TEXT NOT NULL, " +
        COLUMN_DESCRIPTION + " TEXT, " +
        COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0, " +
        COLUMN_CREATED_DATE + " TEXT NOT NULL, " +
        COLUMN_DUE_DATE + " TEXT, " +
        COLUMN_CALENDAR_EVENT_ID + " TEXT, " +
        COLUMN_LOCATION + " TEXT, " +
        COLUMN_CATEGORY + " TEXT" +
        ")";

    public TodoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TODOS);
        Log.d(TAG, "데이터베이스 테이블이 생성되었습니다.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 간단한 업그레이드: 기존 테이블 삭제 후 재생성
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
        onCreate(db);
    }

    // Todo 추가
    public long addTodo(Todo todo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, todo.getTitle());
        values.put(COLUMN_DESCRIPTION, todo.getDescription());
        values.put(COLUMN_IS_COMPLETED, todo.isCompleted() ? 1 : 0);
        values.put(COLUMN_CREATED_DATE, formatDate(todo.getCreatedDate()));
        values.put(COLUMN_DUE_DATE, todo.getDueDate() != null ? formatDate(todo.getDueDate()) : null);
        values.put(COLUMN_CALENDAR_EVENT_ID, todo.getCalendarEventId());
        values.put(COLUMN_LOCATION, todo.getLocation());
        values.put(COLUMN_CATEGORY, todo.getCategory());

        long id = db.insert(TABLE_TODOS, null, values);
        db.close();
        
        Log.d(TAG, "Todo가 추가되었습니다. ID: " + id);
        return id;
    }

    // Todo 업데이트
    public int updateTodo(Todo todo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, todo.getTitle());
        values.put(COLUMN_DESCRIPTION, todo.getDescription());
        values.put(COLUMN_IS_COMPLETED, todo.isCompleted() ? 1 : 0);
        values.put(COLUMN_CREATED_DATE, formatDate(todo.getCreatedDate()));
        values.put(COLUMN_DUE_DATE, todo.getDueDate() != null ? formatDate(todo.getDueDate()) : null);
        values.put(COLUMN_CALENDAR_EVENT_ID, todo.getCalendarEventId());
        values.put(COLUMN_LOCATION, todo.getLocation());
        values.put(COLUMN_CATEGORY, todo.getCategory());

        int result = db.update(TABLE_TODOS, values, COLUMN_ID + " = ?", 
                             new String[]{String.valueOf(todo.getId())});
        db.close();
        
        Log.d(TAG, "Todo가 업데이트되었습니다. ID: " + todo.getId() + ", 결과: " + result);
        return result;
    }

    // Todo 삭제
    public int deleteTodo(int todoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TODOS, COLUMN_ID + " = ?", 
                             new String[]{String.valueOf(todoId)});
        db.close();
        
        Log.d(TAG, "Todo가 삭제되었습니다. ID: " + todoId + ", 결과: " + result);
        return result;
    }

    // 모든 Todo 조회
    public List<Todo> getAllTodos() {
        List<Todo> todoList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TODOS + " ORDER BY " + COLUMN_CREATED_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Todo todo = cursorToTodo(cursor);
                todoList.add(todo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        
        Log.d(TAG, "모든 Todo를 조회했습니다. 개수: " + todoList.size());
        return todoList;
    }

    // ID로 Todo 조회
    public Todo getTodoById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TODOS, null, COLUMN_ID + " = ?", 
                                new String[]{String.valueOf(id)}, null, null, null);

        Todo todo = null;
        if (cursor.moveToFirst()) {
            todo = cursorToTodo(cursor);
        }

        cursor.close();
        db.close();
        return todo;
    }

    // 완료된 Todo만 조회
    public List<Todo> getCompletedTodos() {
        List<Todo> todoList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TODOS + 
                           " WHERE " + COLUMN_IS_COMPLETED + " = 1" +
                           " ORDER BY " + COLUMN_CREATED_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Todo todo = cursorToTodo(cursor);
                todoList.add(todo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return todoList;
    }

    // 미완료 Todo만 조회
    public List<Todo> getIncompleteTodos() {
        List<Todo> todoList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TODOS + 
                           " WHERE " + COLUMN_IS_COMPLETED + " = 0" +
                           " ORDER BY " + COLUMN_CREATED_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Todo todo = cursorToTodo(cursor);
                todoList.add(todo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return todoList;
    }

    // 카테고리별 Todo 조회
    public List<Todo> getTodosByCategory(String category) {
        List<Todo> todoList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TODOS + 
                           " WHERE " + COLUMN_CATEGORY + " = ?" +
                           " ORDER BY " + COLUMN_CREATED_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{category});

        if (cursor.moveToFirst()) {
            do {
                Todo todo = cursorToTodo(cursor);
                todoList.add(todo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return todoList;
    }

    // 모든 Todo 삭제
    public void clearAllTodos() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODOS, null, null);
        db.close();
        Log.d(TAG, "모든 Todo가 삭제되었습니다.");
    }

    // Cursor를 Todo 객체로 변환
    private Todo cursorToTodo(Cursor cursor) {
        Todo todo = new Todo();
        todo.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
        todo.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
        todo.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
        todo.setCompleted(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_COMPLETED)) == 1);
        todo.setCreatedDate(parseDate(cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_DATE))));
        
        String dueDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_DUE_DATE));
        if (dueDateStr != null) {
            todo.setDueDate(parseDate(dueDateStr));
        }
        
        todo.setCalendarEventId(cursor.getString(cursor.getColumnIndex(COLUMN_CALENDAR_EVENT_ID)));
        todo.setLocation(cursor.getString(cursor.getColumnIndex(COLUMN_LOCATION)));
        todo.setCategory(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
        
        return todo;
    }

    // Date를 문자열로 변환
    private String formatDate(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

    // 문자열을 Date로 변환
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (Exception e) {
            Log.e(TAG, "날짜 파싱 오류: " + dateStr, e);
            return null;
        }
    }
} 