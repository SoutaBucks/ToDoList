package com.example.to_do_list;

import java.io.Serializable;
import java.util.Date;

public class Todo implements Serializable {
    private int id;
    private String title;
    private String description;
    private boolean isCompleted;
    private Date createdDate;
    private Date dueDate;
    private String calendarEventId; // Google Calendar 이벤트 ID
    private String location; // 지역 정보

    // 생성자
    public Todo() {
        this.createdDate = new Date();
        this.isCompleted = false;
    }

    public Todo(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public Todo(int id, String title, String description, boolean isCompleted, Date createdDate, Date dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
    }

    public Todo(int id, String title, String description, boolean isCompleted, Date createdDate, Date dueDate, String calendarEventId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
        this.calendarEventId = calendarEventId;
    }

    public Todo(int id, String title, String description, boolean isCompleted, Date createdDate, Date dueDate, String calendarEventId, String location) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
        this.calendarEventId = calendarEventId;
        this.location = location;
    }

    // Getter and Setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(String calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
} 