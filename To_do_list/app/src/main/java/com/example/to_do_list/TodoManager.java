package com.example.to_do_list;

import android.content.Context;
import android.util.Log;
import java.util.List;

public class TodoManager {
    private static final String TAG = "TodoManager";

    private TodoDatabaseHelper databaseHelper;
    private static TodoManager instance;
    private AndroidCalendarManager calendarManager;

    private TodoManager(Context context) {
        databaseHelper = new TodoDatabaseHelper(context.getApplicationContext());
        calendarManager = new AndroidCalendarManager(context);
    }

    public static synchronized TodoManager getInstance(Context context) {
        if (instance == null) {
            instance = new TodoManager(context.getApplicationContext());
        }
        return instance;
    }

    public List<Todo> getAllTodos() {
        return databaseHelper.getAllTodos();
    }

    public void saveTodo(Todo todo) {
        boolean isUpdate = todo.getId() != 0;
        
        if (isUpdate) {
            // 기존 Todo 업데이트
            databaseHelper.updateTodo(todo);
            Log.d(TAG, "Todo 업데이트됨: " + todo.getId());
        } else {
            // 새로운 Todo 추가
            long newId = databaseHelper.addTodo(todo);
            todo.setId((int) newId);
            Log.d(TAG, "새 Todo 추가됨: " + newId);
        }
        
        // Android Calendar와 동기화
        syncTodoWithCalendar(todo, isUpdate);
    }

    public void deleteTodo(Todo todo) {
        databaseHelper.deleteTodo(todo.getId());
        Log.d(TAG, "Todo 삭제됨: " + todo.getId());
        
        // Android Calendar에서도 삭제
        removeTodoFromCalendar(todo);
    }

    public void updateTodo(Todo todo) {
        saveTodo(todo); // saveTodo가 업데이트도 처리함
    }

    public Todo getTodoById(int id) {
        return databaseHelper.getTodoById(id);
    }

    public List<Todo> getCompletedTodos() {
        return databaseHelper.getCompletedTodos();
    }

    public List<Todo> getIncompleteTodos() {
        return databaseHelper.getIncompleteTodos();
    }

    public List<Todo> getTodosByCategory(String category) {
        return databaseHelper.getTodosByCategory(category);
    }

    public void clearAllTodos() {
        databaseHelper.clearAllTodos();
        Log.d(TAG, "모든 Todo가 삭제되었습니다.");
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