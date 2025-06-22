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
        editTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // 사용자가 입력을 멈췄을 때만 분류를 실행하기 위한 간단한 디바운싱
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> classifyTitle(s.toString()), 500);
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
        if (scheduleClassifier == null) return;
        if (title.trim().isEmpty()) return;

        executor.execute(() -> {
            try {
                final String category = scheduleClassifier.classify(title);
                handler.post(() -> {
                    setSpinnerToValue(category);
                });
            } catch (Exception e) {
                Log.e("AddTodoActivity", "Error classifying text", e);
            }
        });
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


