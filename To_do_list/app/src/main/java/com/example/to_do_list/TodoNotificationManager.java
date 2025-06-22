package com.example.to_do_list;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.Date;

public class TodoNotificationManager {
    private static final String TAG = "TodoNotificationManager";
    private static final String CHANNEL_ID = "todo_notifications";
    private static final String CHANNEL_NAME = "할일 알림";
    private static final String CHANNEL_DESCRIPTION = "할일 마감일 알림";
    
    private Context context;
    private NotificationManagerCompat notificationManager;
    
    public TodoNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }
    
    /**
     * 알림 채널 생성 (Android 8.0 이상 필요)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            
            Log.d(TAG, "알림 채널이 생성되었습니다: " + CHANNEL_ID);
        }
    }
    
    /**
     * 즉시 알림 표시
     */
    public void showNotification(Todo todo, String title, String message) {
        Intent intent = new Intent(context, TodoDetailActivity.class);
        intent.putExtra("todo", todo);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            todo.getId(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 1000, 500, 1000});
        
        notificationManager.notify(todo.getId(), builder.build());
        Log.d(TAG, "알림이 표시되었습니다: " + title);
    }
    
    /**
     * 마감일 알림 스케줄링
     */
    public void scheduleDueDateNotification(Todo todo) {
        if (todo.getDueDate() == null) {
            Log.d(TAG, "마감일이 없어 알림을 설정하지 않습니다: " + todo.getTitle());
            return;
        }
        
        // 마감일 1시간 전에 알림
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.setTime(todo.getDueDate());
        notificationTime.add(Calendar.HOUR_OF_DAY, -1);
        
        // 현재 시간보다 이후여야 함
        if (notificationTime.getTimeInMillis() <= System.currentTimeMillis()) {
            Log.d(TAG, "알림 시간이 과거입니다. 스케줄링하지 않습니다: " + todo.getTitle());
            return;
        }
        
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("todo_id", todo.getId());
        intent.putExtra("todo_title", todo.getTitle());
        intent.putExtra("todo_description", todo.getDescription());
        
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
            context,
            todo.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime.getTimeInMillis(),
                alarmIntent
            );
            
            Log.d(TAG, "마감일 알림이 스케줄되었습니다: " + todo.getTitle() + 
                  " at " + notificationTime.getTime());
        }
    }
    
    /**
     * 알림 취소
     */
    public void cancelNotification(Todo todo) {
        // 기존 알림 취소
        notificationManager.cancel(todo.getId());
        
        // 스케줄된 알람 취소
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
            context,
            todo.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
            Log.d(TAG, "알림이 취소되었습니다: " + todo.getTitle());
        }
    }
    
    /**
     * 완료된 할일 축하 알림
     */
    public void showCompletionNotification(Todo todo) {
        String title = "할일 완료! 🎉";
        String message = "'" + todo.getTitle() + "'을(를) 완료하셨습니다!";
        
        showNotification(todo, title, message);
    }
    
    /**
     * 오늘 마감인 할일들 알림
     */
    public void showTodayDueTodosNotification(java.util.List<Todo> todayDueTodos) {
        if (todayDueTodos.isEmpty()) return;
        
        String title = "오늘 마감인 할일 " + todayDueTodos.size() + "개";
        StringBuilder message = new StringBuilder();
        
        for (int i = 0; i < Math.min(todayDueTodos.size(), 3); i++) {
            if (i > 0) message.append("\n");
            message.append("• ").append(todayDueTodos.get(i).getTitle());
        }
        
        if (todayDueTodos.size() > 3) {
            message.append("\n외 ").append(todayDueTodos.size() - 3).append("개 더");
        }
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            999, // 고유 ID
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.toString()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 1000, 500, 1000});
        
        notificationManager.notify(999, builder.build());
        Log.d(TAG, "오늘 마감 할일 알림이 표시되었습니다: " + todayDueTodos.size() + "개");
    }
    
    /**
     * 알림 권한 확인
     */
    public boolean hasNotificationPermission() {
        return notificationManager.areNotificationsEnabled();
    }
    
    /**
     * 알림 BroadcastReceiver
     */
    public static class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int todoId = intent.getIntExtra("todo_id", -1);
            String todoTitle = intent.getStringExtra("todo_title");
            String todoDescription = intent.getStringExtra("todo_description");
            
            if (todoId != -1 && todoTitle != null) {
                TodoNotificationManager manager = new TodoNotificationManager(context);
                
                // 더미 Todo 객체 생성 (알림용)
                Todo dummyTodo = new Todo(todoId, todoTitle, todoDescription, false, new Date(), null, null, null);
                
                String notificationTitle = "마감일 알림 ⏰";
                String notificationMessage = "'" + todoTitle + "'의 마감일이 1시간 남았습니다!";
                
                manager.showNotification(dummyTodo, notificationTitle, notificationMessage);
                
                Log.d("NotificationReceiver", "마감일 알림이 전송되었습니다: " + todoTitle);
            }
        }
    }
} 