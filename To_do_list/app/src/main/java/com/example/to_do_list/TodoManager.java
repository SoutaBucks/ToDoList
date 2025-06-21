package com.example.to_do_list;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TodoManager {
    private static final String PREF_NAME = "todo_prefs";
    private static final String KEY_TODOS = "todos";
    private static final String KEY_LAST_ID = "last_id";
    private static final String TAG = "TodoManager";

    private SharedPreferences sharedPreferences;
    private Gson gson;
    private static TodoManager instance;
    private AndroidCalendarManager calendarManager;

    private TodoManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        calendarManager = new AndroidCalendarManager(context);
    }

    public static synchronized TodoManager getInstance(Context context) {
        if (instance == null) {
            instance = new TodoManager(context.getApplicationContext());
        }
        return instance;
    }

    public List<Todo> getAllTodos() {
        String todosJson = sharedPreferences.getString(KEY_TODOS, "");
        if (todosJson.isEmpty()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<Todo>>(){}.getType();
        List<Todo> todos = gson.fromJson(todosJson, listType);
        return todos != null ? todos : new ArrayList<>();
    }

    public void saveTodo(Todo todo) {
        List<Todo> todos = getAllTodos();
        
        // ID가 없으면 새로운 ID 할당
        if (todo.getId() == 0) {
            int lastId = sharedPreferences.getInt(KEY_LAST_ID, 0);
            todo.setId(++lastId);
            sharedPreferences.edit().putInt(KEY_LAST_ID, lastId).apply();
        }

        // 기존 TODO가 있으면 업데이트, 없으면 추가
        boolean found = false;
        for (int i = 0; i < todos.size(); i++) {
            if (todos.get(i).getId() == todo.getId()) {
                todos.set(i, todo);
                found = true;
                break;
            }
        }
        
        if (!found) {
            todos.add(0, todo); // 맨 앞에 추가
        }

        saveTodos(todos);
        
        // Android Calendar와 동기화
        syncTodoWithCalendar(todo, found);
    }

    public void deleteTodo(Todo todo) {
        List<Todo> todos = getAllTodos();
        todos.removeIf(t -> t.getId() == todo.getId());
        saveTodos(todos);
        
        // Android Calendar에서도 삭제
        removeTodoFromCalendar(todo);
    }

    public void updateTodo(Todo todo) {
        saveTodo(todo); // saveTodo가 업데이트도 처리함
    }

    private void saveTodos(List<Todo> todos) {
        String todosJson = gson.toJson(todos);
        sharedPreferences.edit().putString(KEY_TODOS, todosJson).apply();
    }

    public Todo getTodoById(int id) {
        List<Todo> todos = getAllTodos();
        for (Todo todo : todos) {
            if (todo.getId() == id) {
                return todo;
            }
        }
        return null;
    }

    public void clearAllTodos() {
        sharedPreferences.edit()
                .remove(KEY_TODOS)
                .remove(KEY_LAST_ID)
                .apply();
    }

    // Android Calendar 동기화 메서드들
    private void syncTodoWithCalendar(Todo todo, boolean isUpdate) {
        if (!calendarManager.isCalendarAvailable()) {
            Log.d(TAG, "Android 캘린더가 사용 불가능하여 동기화를 건너뜁니다.");
            return;
        }

        new Thread(() -> {
            try {
                if (isUpdate) {
                    // 기존 Todo 업데이트
                    String result = calendarManager.updateTodoInCalendar(todo);
                    Log.d(TAG, "캘린더 업데이트 결과: " + result);
                } else {
                    // 새로운 Todo 추가
                    String result = calendarManager.addTodoToCalendar(todo);
                    Log.d(TAG, "캘린더 추가 결과: " + result);
                }
            } catch (Exception e) {
                Log.e(TAG, "캘린더 동기화 실패: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void removeTodoFromCalendar(Todo todo) {
        if (!calendarManager.isCalendarAvailable()) {
            Log.d(TAG, "Android 캘린더가 사용 불가능하여 삭제를 건너뜁니다.");
            return;
        }

        new Thread(() -> {
            try {
                String result = calendarManager.removeTodoFromCalendar(todo);
                Log.d(TAG, "캘린더 삭제 결과: " + result);
            } catch (Exception e) {
                Log.e(TAG, "캘린더 삭제 실패: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // 모든 Todo를 캘린더와 동기화
    public void syncAllTodosWithCalendar() {
        if (!calendarManager.isCalendarAvailable()) {
            Log.d(TAG, "Android 캘린더가 사용 불가능하여 전체 동기화를 건너뜁니다.");
            return;
        }

        new Thread(() -> {
            try {
                List<Todo> todos = getAllTodos();
                String result = calendarManager.syncAllTodosToCalendar(todos);
                Log.d(TAG, "전체 동기화 결과: " + result);
            } catch (Exception e) {
                Log.e(TAG, "전체 동기화 실패: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public AndroidCalendarManager getCalendarManager() {
        return calendarManager;
    }
} 