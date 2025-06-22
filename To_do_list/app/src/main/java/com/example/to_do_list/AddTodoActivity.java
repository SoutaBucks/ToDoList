package com.example.to_do_list;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTodoActivity extends AppCompatActivity {
    
    private EditText editTextTitle;
    private EditText editTextDescription;
    private TextView textViewDueDate;
    private AutoCompleteTextView autoCompleteLocation;
    private CheckBox checkBoxCompleted;
    private Button buttonSave;
    private Button buttonCancel;
    private Button buttonSelectDate;
    private Spinner spinnerCategory;
    
    private Date selectedDueDate;
    private SimpleDateFormat dateFormat;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ScheduleClassifier scheduleClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        
        initializeViews();
        setupCategorySpinner();
        setupClassifier();
        setupDateFormat();
        setupListeners();
        setupLocationAutoComplete();
    }

    private void initializeViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        textViewDueDate = findViewById(R.id.textViewDueDate);
        autoCompleteLocation = findViewById(R.id.autoCompleteTextViewLocation);
        checkBoxCompleted = findViewById(R.id.checkBoxCompleted);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this,
            R.array.todo_categories,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupClassifier() {
        try {
            scheduleClassifier = new ScheduleClassifier(this);
        } catch (Exception e) {
            Toast.makeText(this, "분류기 초기화 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("AddTodoActivity", "Error initializing classifier", e);
        }
    }

    private void setupDateFormat() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        textViewDueDate.setText("날짜 선택 안함");
    }

    private void setupListeners() {
        editTextTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // 포커스를 잃었을 때 (입력 완료 후) 분류 실행
                if (!hasFocus) {
                    String title = editTextTitle.getText().toString().trim();
                    if (!title.isEmpty()) {
                        classifyTitle(title);
                    }
                }
            }
        });

        // 설명 필드에 포커스가 갔을 때도 제목 분류
        editTextDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String title = editTextTitle.getText().toString().trim();
                    if (!title.isEmpty()) {
                        classifyTitle(title);
                    }
                }
            }
        });

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

    private void setupLocationAutoComplete() {
        String[] locations = getResources().getStringArray(R.array.kma_locations);
        LocationArrayAdapter adapter = new LocationArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>(Arrays.asList(locations)));
        autoCompleteLocation.setAdapter(adapter);
    }

    private static class LocationArrayAdapter extends ArrayAdapter<String> {
        private final ArrayList<String> allLocations;
        private final Filter locationFilter;

        public LocationArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> locations) {
            super(context, resource, locations);
            this.allLocations = new ArrayList<>(locations);
            this.locationFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    ArrayList<String> suggestions = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) {
                        suggestions.addAll(allLocations);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        String chosungPattern = HangulUtils.getChosung(filterPattern);

                        for (String location : allLocations) {
                            if (location.toLowerCase().contains(filterPattern)) {
                                suggestions.add(location);
                            } else if (HangulUtils.getChosung(location).contains(chosungPattern)) {
                                suggestions.add(location);
                            }
                        }
                    }
                    results.values = suggestions;
                    results.count = suggestions.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, @NonNull FilterResults results) {
                    clear();
                    if (results.values != null) {
                        addAll((ArrayList<String>) results.values);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    return (String) resultValue;
                }
            };
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return locationFilter;
        }
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
        String location = autoCompleteLocation.getText().toString().trim();
        boolean isCompleted = checkBoxCompleted.isChecked();

        if (title.isEmpty()) {
            editTextTitle.setError("제목을 입력해주세요");
            editTextTitle.requestFocus();
            return;
        }

        Todo newTodo = new Todo(title, description);
        newTodo.setCompleted(isCompleted);
        newTodo.setDueDate(selectedDueDate);
        newTodo.setLocation(location);
        
        // 선택된 카테고리 저장
        String selectedCategory = spinnerCategory.getSelectedItem().toString();
        newTodo.setCategory(selectedCategory);

        // Executor를 사용하여 백그라운드에서 데이터 저장
        executor.execute(() -> {
            TodoManager todoManager = TodoManager.getInstance(getApplicationContext());
            todoManager.saveTodo(newTodo);

            // UI 업데이트는 메인 스레드에서
            handler.post(() -> {
                setResult(RESULT_OK);
                Toast.makeText(this, "할일이 저장되었습니다", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void classifyTitle(String title) {
        if (title.trim().isEmpty()) return;

        Log.d("AddTodoActivity", "Classifying title: " + title);
        
        // 우선 키워드 기반 분류 시도
        String keywordCategory = classifyByKeywords(title);
        if (!keywordCategory.equals("기타")) {
            setSpinnerToValue(keywordCategory);
            Toast.makeText(this, "카테고리 분류: " + keywordCategory, Toast.LENGTH_SHORT).show();
            return;
        }

        // 키워드 분류 실패 시 ML 모델 시도
        if (scheduleClassifier != null) {
            executor.execute(() -> {
                try {
                    final String category = scheduleClassifier.classify(title);
                    Log.d("AddTodoActivity", "ML Classified as: " + category);
                    handler.post(() -> {
                        setSpinnerToValue(category);
                        Toast.makeText(this, "카테고리 자동 분류: " + category, Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    Log.e("AddTodoActivity", "Error classifying text: " + title, e);
                    handler.post(() -> {
                        setSpinnerToValue("기타");
                        Toast.makeText(this, "기본 카테고리로 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            setSpinnerToValue("기타");
            Toast.makeText(this, "기본 카테고리로 설정되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private String classifyByKeywords(String title) {
        String lowerTitle = title.toLowerCase();
        
        // 학업 관련 키워드
        if (lowerTitle.contains("과제") || lowerTitle.contains("숙제") || lowerTitle.contains("공부") || 
            lowerTitle.contains("시험") || lowerTitle.contains("학습") || lowerTitle.contains("수학") || 
            lowerTitle.contains("영어") || lowerTitle.contains("머신러닝") || lowerTitle.contains("프로그래밍")) {
            return "학업";
        }
        
        // 운동 관련 키워드
        if (lowerTitle.contains("운동") || lowerTitle.contains("헬스") || lowerTitle.contains("조깅") || 
            lowerTitle.contains("수영") || lowerTitle.contains("축구") || lowerTitle.contains("농구") || 
            lowerTitle.contains("요가") || lowerTitle.contains("피트니스")) {
            return "운동";
        }
        
        // 업무 관련 키워드
        if (lowerTitle.contains("회의") || lowerTitle.contains("업무") || lowerTitle.contains("프로젝트") || 
            lowerTitle.contains("보고서") || lowerTitle.contains("발표") || lowerTitle.contains("미팅") || 
            lowerTitle.contains("일") || lowerTitle.contains("직장")) {
            return "업무";
        }
        
        // 만남 관련 키워드
        if (lowerTitle.contains("만남") || lowerTitle.contains("약속") || lowerTitle.contains("데이트") || 
            lowerTitle.contains("친구") || lowerTitle.contains("모임") || lowerTitle.contains("파티") || 
            lowerTitle.contains("식사") || lowerTitle.contains("커피")) {
            return "만남";
        }
        
        // 금융 관련 키워드
        if (lowerTitle.contains("은행") || lowerTitle.contains("돈") || lowerTitle.contains("결제") || 
            lowerTitle.contains("카드") || lowerTitle.contains("투자") || lowerTitle.contains("적금") || 
            lowerTitle.contains("대출") || lowerTitle.contains("세금")) {
            return "금융";
        }
        
        // 중요 관련 키워드
        if (lowerTitle.contains("중요") || lowerTitle.contains("긴급") || lowerTitle.contains("필수") || 
            lowerTitle.contains("마감") || lowerTitle.contains("데드라인")) {
            return "중요";
        }
        
        return "기타";
    }

    private void setSpinnerToValue(String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerCategory.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }
} 


