package com.example.to_do_list;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TodoDetailActivity extends AppCompatActivity {
    
    private EditText editTextTitle;
    private EditText editTextDescription;
    private TextView textViewCreatedDate;
    private TextView textViewCategory;
    private TextView textViewDueDate;
    private TextView labelWeather;
    private TextView textViewWeather;
    private CheckBox checkBoxCompleted;
    private Button buttonEdit;
    private Button buttonDelete;
    private Button buttonSave;
    private Button buttonCancel;
    private Button buttonSelectDate;
    
    private Todo currentTodo;
    private boolean isEditMode = false;
    private Date selectedDueDate;
    private SimpleDateFormat dateFormat;
    private TodoManager todoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);
        
        todoManager = TodoManager.getInstance(this);
        initializeViews();
        setupDateFormat();
        loadTodoData();
        setupListeners();
        setEditMode(false);
    }

    private void initializeViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        textViewCreatedDate = findViewById(R.id.textViewCreatedDate);
        textViewCategory = findViewById(R.id.textViewCategory);
        textViewDueDate = findViewById(R.id.textViewDueDate);
        labelWeather = findViewById(R.id.labelWeather);
        textViewWeather = findViewById(R.id.textViewWeather);
        checkBoxCompleted = findViewById(R.id.checkBoxCompleted);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
    }

    private void setupDateFormat() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    private void loadTodoData() {
        Intent intent = getIntent();
        currentTodo = (Todo) intent.getSerializableExtra("todo");
        
        if (currentTodo != null) {
            editTextTitle.setText(currentTodo.getTitle());
            editTextDescription.setText(currentTodo.getDescription());
            checkBoxCompleted.setChecked(currentTodo.isCompleted());
            
            if (currentTodo.getCreatedDate() != null) {
                textViewCreatedDate.setText("생성일: " + dateFormat.format(currentTodo.getCreatedDate()));
            }
            
            // 카테고리 표시
            if (currentTodo.getCategory() != null && !currentTodo.getCategory().isEmpty()) {
                textViewCategory.setText("카테고리: " + currentTodo.getCategory());
            } else {
                textViewCategory.setText("카테고리: 기타");
            }
            
            if (currentTodo.getDueDate() != null) {
                textViewDueDate.setText("마감일: " + dateFormat.format(currentTodo.getDueDate()));
                selectedDueDate = currentTodo.getDueDate();
                
                // 마감일과 위치가 있으면 날씨 정보 로드
                if (currentTodo.getLocation() != null && !currentTodo.getLocation().isEmpty()) {
                    loadWeatherInfo(currentTodo.getLocation());
                }
            } else {
                textViewDueDate.setText("마감일: 설정 안함");
            }
        }
    }

    private void setupListeners() {
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditMode(true);
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTodo();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    setEditMode(false);
                    loadTodoData(); // 원래 데이터로 복원
                } else {
                    finish();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmDialog();
            }
        });

        buttonSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        checkBoxCompleted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (currentTodo != null && !isEditMode) {
                    currentTodo.setCompleted(isChecked);
                    // 실제로 데이터를 업데이트
                    todoManager.updateTodo(currentTodo);
                    Toast.makeText(TodoDetailActivity.this, 
                        isChecked ? "완료 처리되었습니다" : "미완료로 변경되었습니다", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        
        editTextTitle.setEnabled(editMode);
        editTextDescription.setEnabled(editMode);
        buttonSelectDate.setVisibility(editMode ? View.VISIBLE : View.GONE);
        
        if (editMode) {
            buttonEdit.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
            buttonSave.setVisibility(View.VISIBLE);
            buttonCancel.setText("취소");
        } else {
            buttonEdit.setVisibility(View.VISIBLE);
            buttonDelete.setVisibility(View.VISIBLE);
            buttonSave.setVisibility(View.GONE);
            buttonCancel.setText("닫기");
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDueDate != null) {
            calendar.setTime(selectedDueDate);
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDueDate = selectedCalendar.getTime();
                    textViewDueDate.setText("마감일: " + dateFormat.format(selectedDueDate));
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void saveTodo() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (title.isEmpty()) {
            editTextTitle.setError("제목을 입력해주세요");
            editTextTitle.requestFocus();
            return;
        }

        currentTodo.setTitle(title);
        currentTodo.setDescription(description);
        currentTodo.setDueDate(selectedDueDate);

        // 실제로 데이터를 업데이트
        todoManager.updateTodo(currentTodo);

        Toast.makeText(this, "할일이 수정되었습니다", Toast.LENGTH_SHORT).show();
        setEditMode(false);
        
        // 결과를 MainActivity로 전달
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updated", true);
        setResult(RESULT_OK, resultIntent);
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("삭제 확인")
                .setMessage("이 할일을 삭제하시겠습니까?")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTodo();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteTodo() {
        // 실제로 데이터를 삭제
        todoManager.deleteTodo(currentTodo);

        Toast.makeText(this, "할일이 삭제되었습니다", Toast.LENGTH_SHORT).show();
        
        // 결과를 MainActivity로 전달
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleted", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void loadWeatherInfo(String location) {
        // 날씨 UI 표시
        labelWeather.setVisibility(View.VISIBLE);
        textViewWeather.setVisibility(View.VISIBLE);
        textViewWeather.setText("날씨: 로딩 중...");
        
        WeatherManagerKt weatherManager = WeatherManagerKt.Companion.getInstance();
        weatherManager.getWeatherForLocation(location, new WeatherManagerKt.WeatherCallback() {
            @Override
            public void onSuccess(String weatherInfo) {
                runOnUiThread(() -> {
                    textViewWeather.setText("날씨: " + weatherInfo);
                });
            }
            
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    textViewWeather.setText("날씨: " + error);
                });
            }
        });
    }
} 