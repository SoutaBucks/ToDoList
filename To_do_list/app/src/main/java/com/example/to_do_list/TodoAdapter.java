package com.example.to_do_list;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    
    private List<Todo> todoList;
    private OnTodoClickListener listener;
    private SimpleDateFormat dateFormat;
    private TodoManager todoManager;

    public interface OnTodoClickListener {
        void onTodoClick(Todo todo);
        void onTodoDelete(Todo todo);
        void onTodoToggle(Todo todo);
    }

    public TodoAdapter() {
        this.todoList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public void setOnTodoClickListener(OnTodoClickListener listener) {
        this.listener = listener;
    }

    public void setTodoManager(TodoManager todoManager) {
        this.todoManager = todoManager;
    }

    public void setTodoList(List<Todo> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }

    public void addTodo(Todo todo) {
        todoList.add(0, todo); // 맨 위에 추가
        notifyItemInserted(0);
    }

    public void removeTodo(Todo todo) {
        int position = todoList.indexOf(todo);
        if (position != -1) {
            todoList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateTodo(Todo todo) {
        int position = findTodoPosition(todo.getId());
        if (position != -1) {
            todoList.set(position, todo);
            notifyItemChanged(position);
        }
    }

    private int findTodoPosition(int todoId) {
        for (int i = 0; i < todoList.size(); i++) {
            if (todoList.get(i).getId() == todoId) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        holder.bind(todo);
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private LinearLayout mainLayout;
        private TextView textViewTitle;
        private TextView textViewDescription;
        private TextView textViewDueDate;
        private TextView textViewDday;
        private CheckBox checkBoxCompleted;
        private ImageButton buttonDelete;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDueDate = itemView.findViewById(R.id.textViewDueDate);
            textViewDday = itemView.findViewById(R.id.textViewDday);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            // 클릭 리스너 설정
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTodoClick(todoList.get(position));
                    }
                }
            });

            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTodoDelete(todoList.get(position));
                    }
                }
            });

            checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null && buttonView.isPressed()) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Todo todo = todoList.get(position);
                        todo.setCompleted(isChecked);
                        
                        // 실제로 데이터를 업데이트
                        if (todoManager != null) {
                            todoManager.updateTodo(todo);
                        }
                        
                        listener.onTodoToggle(todo);
                        updateTextStyle(isChecked);
                    }
                }
            });
        }

        public void bind(Todo todo) {
            textViewTitle.setText(todo.getTitle());
            
            if (todo.getDescription() != null && !todo.getDescription().isEmpty()) {
                textViewDescription.setText(todo.getDescription());
                textViewDescription.setVisibility(View.VISIBLE);
            } else {
                textViewDescription.setVisibility(View.GONE);
            }

            if (todo.getDueDate() != null) {
                textViewDueDate.setText("마감일: " + dateFormat.format(todo.getDueDate()));
                textViewDueDate.setVisibility(View.VISIBLE);
                
                // D-day 계산 및 표시
                long daysUntilDue = getDaysUntilDue(todo.getDueDate());
                if (todo.isCompleted()) {
                    textViewDday.setVisibility(View.GONE);
                } else if (daysUntilDue < 0) {
                    textViewDday.setText("D+" + Math.abs(daysUntilDue));
                    textViewDday.setVisibility(View.VISIBLE);
                } else if (daysUntilDue == 0) {
                    textViewDday.setText("D-day");
                    textViewDday.setVisibility(View.VISIBLE);
                } else {
                    textViewDday.setText("D-" + daysUntilDue);
                    textViewDday.setVisibility(View.VISIBLE);
                }
            } else {
                textViewDueDate.setVisibility(View.GONE);
                textViewDday.setVisibility(View.GONE);
            }

            checkBoxCompleted.setChecked(todo.isCompleted());
            updateTextStyle(todo.isCompleted());
            
            // 마감일에 따른 배경색과 글자색 설정
            updateDueDateColors(todo);
        }

        private void updateTextStyle(boolean isCompleted) {
            if (isCompleted) {
                textViewTitle.setPaintFlags(textViewTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textViewTitle.setAlpha(0.6f);
                textViewDescription.setAlpha(0.6f);
            } else {
                textViewTitle.setPaintFlags(textViewTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                textViewTitle.setAlpha(1.0f);
                textViewDescription.setAlpha(1.0f);
            }
        }

        private void updateDueDateColors(Todo todo) {
            if (todo.getDueDate() == null) {
                // 마감일이 없으면 기본 색상
                setDefaultColors();
                return;
            }

            // 마감일까지 남은 일수 계산
            long daysUntilDue = getDaysUntilDue(todo.getDueDate());
            
            if (todo.isCompleted()) {
                // 완료된 일정은 회색으로 표시
                setCompletedColors();
            } else if (daysUntilDue < 0) {
                // 마감일이 지난 경우 - 진한 빨간색
                setOverdueColors();
            } else if (daysUntilDue == 0) {
                // 오늘 마감 - 빨간색
                setTodayColors();
            } else if (daysUntilDue <= 1) {
                // 내일 마감 - 주황색
                setTomorrowColors();
            } else if (daysUntilDue <= 3) {
                // 3일 이내 - 연한 주황색
                setNearDueColors();
            } else if (daysUntilDue <= 7) {
                // 1주일 이내 - 연한 노란색
                setWeekDueColors();
            } else {
                // 1주일 이상 남음 - 기본 색상
                setDefaultColors();
            }
        }

        private long getDaysUntilDue(Date dueDate) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar due = Calendar.getInstance();
            due.setTime(dueDate);
            due.set(Calendar.HOUR_OF_DAY, 0);
            due.set(Calendar.MINUTE, 0);
            due.set(Calendar.SECOND, 0);
            due.set(Calendar.MILLISECOND, 0);

            long diffInMillis = due.getTimeInMillis() - today.getTimeInMillis();
            return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        }

        private void setDefaultColors() {
            cardView.setCardBackgroundColor(0xFFFFFFFF); // 흰색
            textViewTitle.setTextColor(0xFF000000); // 검은색
            textViewDescription.setTextColor(0xFF666666); // 회색
            textViewDueDate.setTextColor(0xFF666666); // 회색
        }

        private void setCompletedColors() {
            cardView.setCardBackgroundColor(0xFFF0F0F0); // 연한 회색
            textViewTitle.setTextColor(0xFF888888); // 회색
            textViewDescription.setTextColor(0xFF888888); // 회색
            textViewDueDate.setTextColor(0xFF888888); // 회색
        }

        private void setOverdueColors() {
            cardView.setCardBackgroundColor(0xFFD32F2F); // 진한 빨간색
            textViewTitle.setTextColor(0xFFFFFFFF); // 흰색
            textViewDescription.setTextColor(0xFFFFFFFF); // 흰색
            textViewDueDate.setTextColor(0xFFFFFFFF); // 흰색
        }

        private void setTodayColors() {
            cardView.setCardBackgroundColor(0xFFF44336); // 빨간색
            textViewTitle.setTextColor(0xFFFFFFFF); // 흰색
            textViewDescription.setTextColor(0xFFFFFFFF); // 흰색
            textViewDueDate.setTextColor(0xFFFFFFFF); // 흰색
        }

        private void setTomorrowColors() {
            cardView.setCardBackgroundColor(0xFFFF9800); // 주황색
            textViewTitle.setTextColor(0xFFFFFFFF); // 흰색
            textViewDescription.setTextColor(0xFFFFFFFF); // 흰색
            textViewDueDate.setTextColor(0xFFFFFFFF); // 흰색
        }

        private void setNearDueColors() {
            cardView.setCardBackgroundColor(0xFFFFB74D); // 연한 주황색
            textViewTitle.setTextColor(0xFF000000); // 검은색
            textViewDescription.setTextColor(0xFF000000); // 검은색
            textViewDueDate.setTextColor(0xFF000000); // 검은색
        }

        private void setWeekDueColors() {
            cardView.setCardBackgroundColor(0xFFFFF9C4); // 연한 노란색
            textViewTitle.setTextColor(0xFF000000); // 검은색
            textViewDescription.setTextColor(0xFF000000); // 검은색
            textViewDueDate.setTextColor(0xFF000000); // 검은색
        }
    }
} 