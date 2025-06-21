package com.example.to_do_list;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
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
    
    private Date selectedDueDate;
    private SimpleDateFormat dateFormat;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        
        initializeViews();
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
} 


