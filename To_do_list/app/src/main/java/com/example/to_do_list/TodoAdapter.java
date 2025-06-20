package com.example.to_do_list;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    
    private List<Todo> todoList;
    private OnTodoClickListener listener;
    private SimpleDateFormat dateFormat;

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
        private TextView textViewTitle;
        private TextView textViewDescription;
        private TextView textViewDueDate;
        private CheckBox checkBoxCompleted;
        private ImageButton buttonDelete;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDueDate = itemView.findViewById(R.id.textViewDueDate);
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
            } else {
                textViewDueDate.setVisibility(View.GONE);
            }

            checkBoxCompleted.setChecked(todo.isCompleted());
            updateTextStyle(todo.isCompleted());
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
    }
} 