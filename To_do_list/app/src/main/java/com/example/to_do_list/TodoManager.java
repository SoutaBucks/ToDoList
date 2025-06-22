package com.example.to_do_list;

import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

public class TodoManager {
    private static final String TAG = "TodoManager";

    private TodoDatabaseHelper databaseHelper;
    private static TodoManager instance;
    private AndroidCalendarManager calendarManager;
    private TodoNotificationManager notificationManager;

    private TodoManager(Context context) {
        databaseHelper = new TodoDatabaseHelper(context.getApplicationContext());
        calendarManager = new AndroidCalendarManager(context);
        notificationManager = new TodoNotificationManager(context.getApplicationContext());
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
            
            // 기존 알림 취소 후 새로 스케줄링
            notificationManager.cancelNotification(todo);
            if (!todo.isCompleted()) {
                notificationManager.scheduleDueDateNotification(todo);
            }
        } else {
            // 새로운 Todo 추가
            long newId = databaseHelper.addTodo(todo);
            todo.setId((int) newId);
            Log.d(TAG, "새 Todo 추가됨: " + newId);
            
            // 새로운 Todo에 대한 알림 스케줄링
            if (!todo.isCompleted()) {
                notificationManager.scheduleDueDateNotification(todo);
            }
        }
        
        // Android Calendar와 동기화
        syncTodoWithCalendar(todo, isUpdate);
    }

    public void deleteTodo(Todo todo) {
        databaseHelper.deleteTodo(todo.getId());
        Log.d(TAG, "Todo 삭제됨: " + todo.getId());
        
        // 알림 취소
        notificationManager.cancelNotification(todo);
        
        // Android Calendar에서도 삭제
        removeTodoFromCalendar(todo);
    }

    public void updateTodo(Todo todo) {
        saveTodo(todo); // saveTodo가 업데이트도 처리함
    }

    public void toggleTodoCompletion(Todo todo) {
        todo.setCompleted(!todo.isCompleted());
        updateTodo(todo);
        
        if (todo.isCompleted()) {
            // 완료 시 축하 알림 표시
            notificationManager.showCompletionNotification(todo);
            // 기존 마감일 알림 취소
            notificationManager.cancelNotification(todo);
        } else {
            // 미완료로 변경 시 다시 알림 스케줄링
            notificationManager.scheduleDueDateNotification(todo);
        }
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
    
    public List<Todo> getTodayDueTodos() {
        List<Todo> allTodos = getAllTodos();
        List<Todo> todayDueTodos = new ArrayList<>();
        
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);
        
        for (Todo todo : allTodos) {
            if (todo.getDueDate() != null && !todo.isCompleted()) {
                Calendar dueDate = Calendar.getInstance();
                dueDate.setTime(todo.getDueDate());
                
                if (dueDate.getTimeInMillis() >= today.getTimeInMillis() && 
                    dueDate.getTimeInMillis() < tomorrow.getTimeInMillis()) {
                    todayDueTodos.add(todo);
                }
            }
        }
        
        return todayDueTodos;
    }

    public void clearAllTodos() {
        // 모든 알림 취소
        List<Todo> allTodos = getAllTodos();
        for (Todo todo : allTodos) {
            notificationManager.cancelNotification(todo);
        }
        
        databaseHelper.clearAllTodos();
        Log.d(TAG, "모든 Todo가 삭제되었습니다.");
    }

    // 알림 관련 메소드들
    public void showTodayDueTodosNotification() {
        List<Todo> todayDueTodos = getTodayDueTodos();
        if (!todayDueTodos.isEmpty()) {
            notificationManager.showTodayDueTodosNotification(todayDueTodos);
        }
    }
    
    public void rescheduleAllNotifications() {
        List<Todo> incompleteTodos = getIncompleteTodos();
        for (Todo todo : incompleteTodos) {
            notificationManager.scheduleDueDateNotification(todo);
        }
        Log.d(TAG, "모든 알림이 다시 스케줄되었습니다: " + incompleteTodos.size() + "개");
    }
    
    public boolean hasNotificationPermission() {
        return notificationManager.hasNotificationPermission();
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
    
    public TodoNotificationManager getNotificationManager() {
        return notificationManager;
    }
} 