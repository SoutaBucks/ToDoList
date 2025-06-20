package com.example.to_do_list;

import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
    private TextView textViewEmpty;
    private FloatingActionButton fabAddTodo;
    private TodoAdapter todoAdapter;
    private TodoManager todoManager;
    private GoogleCalendarManager calendarManager;

    private ActivityResultLauncher<Intent> addTodoLauncher;
    private ActivityResultLauncher<Intent> detailTodoLauncher;
    private ActivityResultLauncher<Intent> accountPickerLauncher;

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

        initializeViews();
        setupRecyclerView();
        setupActivityLaunchers();
        setupClickListeners();
        loadTodos();
    }

    private void initializeViews() {
        recyclerViewTodos = findViewById(R.id.recyclerViewTodos);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        fabAddTodo = findViewById(R.id.fabAddTodo);
        todoManager = TodoManager.getInstance(this);
        calendarManager = new GoogleCalendarManager(this);
    }

    private void setupRecyclerView() {
        todoAdapter = new TodoAdapter();
        todoAdapter.setOnTodoClickListener(this);
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
                    if (data != null && data.getBooleanExtra("deleted", false)) {
                        Toast.makeText(this, "할일이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "할일이 수정되었습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

        accountPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String accountName = result.getData().getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        calendarManager.getCredential().setSelectedAccountName(accountName);
                        Toast.makeText(this, "Google 계정이 연결되었습니다: " + accountName, Toast.LENGTH_SHORT).show();
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
        
        if (id == R.id.action_connect_google) {
            connectGoogleAccount();
            return true;
        } else if (id == R.id.action_create_calendar) {
            createGoogleCalendar();
            return true;
        } else if (id == R.id.action_sync_calendar) {
            syncTodosToCalendar();
            return true;
        } else if (id == R.id.action_add_test_event) {
            addTestEvent();
            return true;
        } else if (id == R.id.action_view_events) {
            viewEvents();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void connectGoogleAccount() {
        GoogleAccountCredential credential = calendarManager.getCredential();
        Intent intent = credential.newChooseAccountIntent();
        accountPickerLauncher.launch(intent);
    }

    private void createGoogleCalendar() {
        if (!calendarManager.isCredentialSet()) {
            Toast.makeText(this, "먼저 Google 계정을 연결해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("캘린더를 생성하는 중...");
        progressDialog.show();

        new Thread(() -> {
            try {
                String result = calendarManager.createCalendar();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "캘린더 생성 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void syncTodosToCalendar() {
        if (!calendarManager.isCredentialSet()) {
            Toast.makeText(this, "먼저 Google 계정을 연결해주세요", Toast.LENGTH_SHORT).show();
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
                    calendarManager.addTodoToCalendar(todo);
                    successCount++;
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

    private void addTestEvent() {
        if (!calendarManager.isCredentialSet()) {
            Toast.makeText(this, "먼저 Google 계정을 연결해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("테스트 이벤트를 추가하는 중...");
        progressDialog.show();

        new Thread(() -> {
            try {
                String result = calendarManager.addEvent();
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
        if (!calendarManager.isCredentialSet()) {
            Toast.makeText(this, "먼저 Google 계정을 연결해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("이벤트 목록을 가져오는 중...");
        progressDialog.show();

        new Thread(() -> {
            try {
                String result = calendarManager.getEvent();
                List<String> events = calendarManager.getEventStrings();
                
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

    @Override
    protected void onResume() {
        super.onResume();
        loadTodos(); // 화면이 다시 활성화될 때 데이터 새로고침
    }
}