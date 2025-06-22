package com.example.to_do_list;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class DatabaseMigrationHelper {
    private static final String TAG = "DatabaseMigrationHelper";
    private static final String PREF_NAME = "todo_prefs";
    private static final String KEY_TODOS = "todos";
    private static final String KEY_LAST_ID = "last_id";
    private static final String MIGRATION_COMPLETED_KEY = "migration_completed";

    private Context context;
    private SharedPreferences sharedPreferences;
    private TodoDatabaseHelper databaseHelper;
    private Gson gson;

    public DatabaseMigrationHelper(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.databaseHelper = new TodoDatabaseHelper(context);
        this.gson = new Gson();
    }

    /**
     * 마이그레이션이 필요한지 확인하고 필요시 실행
     */
    public void migrateIfNeeded() {
        if (isMigrationCompleted()) {
            Log.d(TAG, "마이그레이션이 이미 완료되었습니다.");
            return;
        }

        Log.d(TAG, "SharedPreferences에서 SQLite로 마이그레이션을 시작합니다.");
        
        try {
            List<Todo> todos = loadTodosFromSharedPreferences();
            if (todos != null && !todos.isEmpty()) {
                migrateTodosToDatabase(todos);
                Log.d(TAG, "마이그레이션 완료: " + todos.size() + "개의 Todo가 이동되었습니다.");
            } else {
                Log.d(TAG, "마이그레이션할 데이터가 없습니다.");
            }
            
            // 마이그레이션 완료 표시
            markMigrationCompleted();
            
            // 기존 SharedPreferences 데이터 정리 (선택사항)
            // clearSharedPreferencesData();
            
        } catch (Exception e) {
            Log.e(TAG, "마이그레이션 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * SharedPreferences에서 Todo 목록 로드
     */
    private List<Todo> loadTodosFromSharedPreferences() {
        String todosJson = sharedPreferences.getString(KEY_TODOS, "");
        if (todosJson.isEmpty()) {
            return null;
        }

        try {
            Type listType = new TypeToken<List<Todo>>(){}.getType();
            return gson.fromJson(todosJson, listType);
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences에서 Todo 로드 실패: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Todo 목록을 데이터베이스로 마이그레이션
     */
    private void migrateTodosToDatabase(List<Todo> todos) {
        for (Todo todo : todos) {
            try {
                // ID가 0이면 새로운 ID 할당
                if (todo.getId() == 0) {
                    long newId = databaseHelper.addTodo(todo);
                    todo.setId((int) newId);
                    Log.d(TAG, "새 Todo 마이그레이션: " + todo.getTitle() + " (ID: " + newId + ")");
                } else {
                    // 기존 ID가 있으면 업데이트
                    databaseHelper.updateTodo(todo);
                    Log.d(TAG, "기존 Todo 마이그레이션: " + todo.getTitle() + " (ID: " + todo.getId() + ")");
                }
            } catch (Exception e) {
                Log.e(TAG, "Todo 마이그레이션 실패: " + todo.getTitle() + " - " + e.getMessage(), e);
            }
        }
    }

    /**
     * 마이그레이션 완료 여부 확인
     */
    private boolean isMigrationCompleted() {
        return sharedPreferences.getBoolean(MIGRATION_COMPLETED_KEY, false);
    }

    /**
     * 마이그레이션 완료 표시
     */
    private void markMigrationCompleted() {
        sharedPreferences.edit()
                .putBoolean(MIGRATION_COMPLETED_KEY, true)
                .apply();
    }

    /**
     * 기존 SharedPreferences 데이터 정리 (선택사항)
     */
    private void clearSharedPreferencesData() {
        sharedPreferences.edit()
                .remove(KEY_TODOS)
                .remove(KEY_LAST_ID)
                .apply();
        Log.d(TAG, "기존 SharedPreferences 데이터가 정리되었습니다.");
    }

    /**
     * 마이그레이션 상태 확인
     */
    public boolean isMigrationNeeded() {
        String todosJson = sharedPreferences.getString(KEY_TODOS, "");
        return !todosJson.isEmpty() && !isMigrationCompleted();
    }

    /**
     * 마이그레이션 통계 정보 반환
     */
    public MigrationStats getMigrationStats() {
        String todosJson = sharedPreferences.getString(KEY_TODOS, "");
        int sharedPrefCount = 0;
        
        if (!todosJson.isEmpty()) {
            try {
                Type listType = new TypeToken<List<Todo>>(){}.getType();
                List<Todo> todos = gson.fromJson(todosJson, listType);
                sharedPrefCount = todos != null ? todos.size() : 0;
            } catch (Exception e) {
                Log.e(TAG, "마이그레이션 통계 계산 실패: " + e.getMessage(), e);
            }
        }

        int databaseCount = databaseHelper.getAllTodos().size();
        boolean migrationCompleted = isMigrationCompleted();

        return new MigrationStats(sharedPrefCount, databaseCount, migrationCompleted);
    }

    /**
     * 마이그레이션 통계 정보 클래스
     */
    public static class MigrationStats {
        public final int sharedPrefCount;
        public final int databaseCount;
        public final boolean migrationCompleted;

        public MigrationStats(int sharedPrefCount, int databaseCount, boolean migrationCompleted) {
            this.sharedPrefCount = sharedPrefCount;
            this.databaseCount = databaseCount;
            this.migrationCompleted = migrationCompleted;
        }

        @Override
        public String toString() {
            return String.format("SharedPreferences: %d개, Database: %d개, 마이그레이션 완료: %s", 
                    sharedPrefCount, databaseCount, migrationCompleted ? "예" : "아니오");
        }
    }
} 