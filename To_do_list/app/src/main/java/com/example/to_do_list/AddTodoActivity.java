package com.example.to_do_list;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTodoActivity extends AppCompatActivity {
    
    private EditText editTextTitle;
    private EditText editTextDescription;
    private TextView textViewDueDate;
    private CheckBox checkBoxCompleted;
    private Button buttonSave;
    private Button buttonCancel;
    private Button buttonSelectDate;
    
    private Date selectedDueDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        
        initializeViews();
        setupDateFormat();
        setupListeners();
    }

    private void initializeViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        textViewDueDate = findViewById(R.id.textViewDueDate);
        checkBoxCompleted = findViewById(R.id.checkBoxCompleted);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
    }

    private void setupDateFormat() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        textViewDueDate.setText("날짜 선택 안함");
    }

    private void setupListeners() {
        buttonSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
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
                finish();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDueDate = selectedCalendar.getTime();
                    textViewDueDate.setText(dateFormat.format(selectedDueDate));
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
        boolean isCompleted = checkBoxCompleted.isChecked();

        if (title.isEmpty()) {
            editTextTitle.setError("제목을 입력해주세요");
            editTextTitle.requestFocus();
            return;
        }

        Todo newTodo = new Todo(title, description);
        newTodo.setCompleted(isCompleted);
        newTodo.setDueDate(selectedDueDate);

        // TODO: 실제로 데이터를 저장하는 로직 추가 (데이터베이스, SharedPreferences 등)
        // 예: TodoManager.saveTodo(newTodo);

        Toast.makeText(this, "할일이 저장되었습니다", Toast.LENGTH_SHORT).show();
        finish();
    }
} 