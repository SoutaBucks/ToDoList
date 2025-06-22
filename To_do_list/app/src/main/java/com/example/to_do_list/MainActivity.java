package com.example.to_do_list;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.List;

public class MainActivity extends AppCompatActivity implements TodoAdapter.OnTodoClickListener {

    private RecyclerView recyclerViewTodos;
    private LinearLayout textViewEmpty;
    private FloatingActionButton fabAddTodo;
    private FloatingActionButton fabCalendar;
    private TodoAdapter todoAdapter;
    private TodoManager todoManager;
    private AndroidCalendarManager calendarManager;
    private DatabaseMigrationHelper migrationHelper;

    private ActivityResultLauncher<Intent> addTodoLauncher;
    private ActivityResultLauncher<Intent> detailTodoLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupPermissionLauncher();
        initializeViews();
        setupRecyclerView();
        setupActivityLaunchers();
        setupClickListeners();
        checkCalendarPermissions();
        
        // 데이터베이스 마이그레이션 실행
        performDatabaseMigration();
        
        loadTodos();
    }

    private void performDatabaseMigration() {
        migrationHelper = new DatabaseMigrationHelper(this);
        
        // 백그라운드에서 마이그레이션 실행
        new Thread(() -> {
            try {
                migrationHelper.migrateIfNeeded();
                
                // 마이그레이션 통계 로그 출력
                DatabaseMigrationHelper.MigrationStats stats = migrationHelper.getMigrationStats();
                Log.d("MainActivity", "마이그레이션 통계: " + stats.toString());
                
                // 마이그레이션이 실행되었다면 UI 업데이트
                if (stats.migrationCompleted && stats.sharedPrefCount > 0) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, 
                            stats.sharedPrefCount + "개의 할일이 데이터베이스로 이동되었습니다.", 
                            Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                Log.e("MainActivity", "마이그레이션 중 오류 발생: " + e.getMessage(), e);
            }
        }).start();
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                
                if (allGranted) {
                    Toast.makeText(this, "캘린더 권한이 허용되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "캘린더 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void checkCalendarPermissions() {
        String[] permissions = {
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            permissionLauncher.launch(permissions);
        }
    }

    private void initializeViews() {
        recyclerViewTodos = findViewById(R.id.recyclerViewTodos);
        textViewEmpty = findViewById(R.id.emptyStateLayout);
        fabAddTodo = findViewById(R.id.fabAddTodo);
        fabCalendar = findViewById(R.id.fabCalendar);
        todoManager = TodoManager.getInstance(this);
        calendarManager = todoManager.getCalendarManager();
    }

    private void setupRecyclerView() {
        todoAdapter = new TodoAdapter();
        todoAdapter.setOnTodoClickListener(this);
        todoAdapter.setTodoManager(todoManager);
        recyclerViewTodos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTodos.setAdapter(todoAdapter);
    }

    private void setupActivityLaunchers() {
        addTodoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadTodos();
                    Toast.makeText(this, "할일이 추가되었습니다", Toast.LENGTH_SHORT).show();
                }
            }
        );

        detailTodoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadTodos();
                    Intent data = result.getData();
                    if (data != null) {
                        if (data.getBooleanExtra("deleted", false)) {
                            Toast.makeText(this, "할일이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                        } else if (data.getBooleanExtra("updated", false)) {
                            Toast.makeText(this, "할일이 수정되었습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    private void setupClickListeners() {
        fabAddTodo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
            addTodoLauncher.launch(intent);
        });

        fabCalendar.setOnClickListener(v -> {
            openGoogleCalendar();
        });
    }

    private void loadTodos() {
        List<Todo> todos = todoManager.getAllTodos();
        todoAdapter.setTodoList(todos);
        
        if (todos.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewTodos.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewTodos.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTodoClick(Todo todo) {
        Intent intent = new Intent(this, TodoDetailActivity.class);
        intent.putExtra("todo", todo);
        detailTodoLauncher.launch(intent);
    }

    @Override
    public void onTodoDelete(Todo todo) {
        new AlertDialog.Builder(this)
            .setTitle("할일 삭제")
            .setMessage("정말로 이 할일을 삭제하시겠습니까?")
            .setPositiveButton("삭제", (dialog, which) -> {
                todoManager.deleteTodo(todo);
                todoAdapter.removeTodo(todo);
                loadTodos(); // 빈 상태 체크를 위해 다시 로드
                Toast.makeText(this, "할일이 삭제되었습니다", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("취소", null)
            .show();
    }

    @Override
    public void onTodoToggle(Todo todo) {
        todoManager.updateTodo(todo);
        String message = todo.isCompleted() ? "할일이 완료되었습니다" : "할일이 미완료로 변경되었습니다";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_sync_calendar) {
            syncTodosToCalendar();
            return true;
        } else if (id == R.id.action_sync_all_todos) {
            syncAllTodosWithCalendar();
            return true;
        } else if (id == R.id.action_add_test_event) {
            addTestEvent();
            return true;
        } else if (id == R.id.action_view_events) {
            viewEvents();
            return true;
        } else if (id == R.id.action_database_info) {
            showDatabaseInfo();
            return true;
        } else if (id == R.id.action_clear_database) {
            showClearDatabaseDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void syncTodosToCalendar() {
        if (!calendarManager.isCalendarAvailable()) {
            Toast.makeText(this, "사용 가능한 캘린더가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Todo> todos = todoManager.getAllTodos();
        if (todos.isEmpty()) {
            Toast.makeText(this, "동기화할 할일이 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("캘린더에 동기화하는 중...");
        progressDialog.show();

        new Thread(() -> {
            int successCount = 0;
            for (Todo todo : todos) {
                try {
                    String result = calendarManager.addTodoToCalendar(todo);
                    if (result.contains("추가되었습니다")) {
                        successCount++;
                    }
                } catch (Exception e) {
                    // 개별 항목 실패는 계속 진행
                }
            }

            final int finalSuccessCount = successCount;
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, 
                    finalSuccessCount + "개의 할일이 캘린더에 동기화되었습니다", 
                    Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    private void syncAllTodosWithCalendar() {
        if (!calendarManager.isCalendarAvailable()) {
            Toast.makeText(this, "사용 가능한 캘린더가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Todo> todos = todoManager.getAllTodos();
        if (todos.isEmpty()) {
            Toast.makeText(this, "동기화할 할일이 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("캘린더에 동기화하는 중...");
        progressDialog.show();

        new Thread(() -> {
            String result = calendarManager.syncAllTodosToCalendar(todos);
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    private void addTestEvent() {
        if (!calendarManager.isCalendarAvailable()) {
            Toast.makeText(this, "사용 가능한 캘린더가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("테스트 이벤트를 추가하는 중...");
        progressDialog.show();

        new Thread(() -> {
            try {
                Todo testTodo = new Todo("테스트 할일", "Android Calendar API 테스트");
                String result = calendarManager.addTodoToCalendar(testTodo);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "테스트 이벤트 추가 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void viewEvents() {
        if (!calendarManager.isCalendarAvailable()) {
            Toast.makeText(this, "사용 가능한 캘린더가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("이벤트 목록을 가져오는 중...");
        progressDialog.show();

        new Thread(() -> {
            try {
                List<String> events = calendarManager.getCalendarEvents();
                String result = calendarManager.getCalendarInfo();
                
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    
                    if (events.isEmpty()) {
                        Toast.makeText(this, "가져온 이벤트가 없습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        // 이벤트 목록을 다이얼로그로 표시
                        StringBuilder eventList = new StringBuilder();
                        for (String event : events) {
                            eventList.append(event).append("\n\n");
                        }
                        
                        new AlertDialog.Builder(this)
                            .setTitle("캘린더 이벤트 목록")
                            .setMessage(eventList.toString())
                            .setPositiveButton("확인", null)
                            .show();
                    }
                    
                    Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "이벤트 목록 가져오기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void openGoogleCalendar() {
        String packageName = "com.google.android.calendar";
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        
        if (intent != null) {
            // Google Calendar 앱 실행
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.d("Calendar", "Google Calendar 앱 실행됨");
        } else {
            // Google Calendar 앱이 없으면 Play Store로 이동
            new AlertDialog.Builder(this)
                .setTitle("Google Calendar 없음")
                .setMessage("Google Calendar 앱이 설치되어 있지 않습니다. Google Play Store에서 설치하시겠습니까?")
                .setPositiveButton("설치", (dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                    }
                })
                .setNegativeButton("취소", null)
                .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodos(); // 화면이 다시 활성화될 때 데이터 새로고침
    }

    private void showDatabaseInfo() {
        if (migrationHelper == null) {
            migrationHelper = new DatabaseMigrationHelper(this);
        }
        
        DatabaseMigrationHelper.MigrationStats stats = migrationHelper.getMigrationStats();
        List<Todo> allTodos = todoManager.getAllTodos();
        List<Todo> completedTodos = todoManager.getCompletedTodos();
        List<Todo> incompleteTodos = todoManager.getIncompleteTodos();
        
        StringBuilder info = new StringBuilder();
        info.append("📊 데이터베이스 정보\n\n");
        info.append("전체 할일: ").append(allTodos.size()).append("개\n");
        info.append("완료된 할일: ").append(completedTodos.size()).append("개\n");
        info.append("미완료 할일: ").append(incompleteTodos.size()).append("개\n\n");
        info.append("🔄 마이그레이션 상태\n");
        info.append(stats.toString());
        
        new AlertDialog.Builder(this)
            .setTitle("데이터베이스 정보")
            .setMessage(info.toString())
            .setPositiveButton("확인", null)
            .show();
    }

    private void showClearDatabaseDialog() {
        new AlertDialog.Builder(this)
            .setTitle("모든 데이터 삭제")
            .setMessage("정말로 모든 할일 데이터를 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("삭제", (dialog, which) -> {
                clearAllData();
            })
            .setNegativeButton("취소", null)
            .show();
    }

    private void clearAllData() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("데이터를 삭제하는 중...");
        progressDialog.show();

        new Thread(() -> {
            try {
                // 데이터베이스에서 모든 Todo 삭제
                todoManager.clearAllTodos();
                
                // 캘린더에서도 삭제 (선택사항)
                if (calendarManager.isCalendarAvailable()) {
                    List<Todo> todos = todoManager.getAllTodos();
                    for (Todo todo : todos) {
                        try {
                            calendarManager.removeTodoFromCalendar(todo);
                        } catch (Exception e) {
                            // 개별 삭제 실패는 무시
                        }
                    }
                }
                
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    loadTodos(); // UI 업데이트
                    Toast.makeText(this, "모든 데이터가 삭제되었습니다", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "데이터 삭제 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}