package com.example.to_do_list;

import android.content.Context;
import android.accounts.Account;
import android.util.Log;

<<<<<<< HEAD
import com.google.api.client.http.javanet.NetHttpTransport;
=======
import com.google.api.client.extensions.android.http.AndroidHttp;
>>>>>>> 0e723050db7ad92052d48a1d8f5f6e5e3e7cd9c2
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoogleCalendarManager {
    private static final String TAG = "GoogleCalendarManager";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    
    private Calendar mService;
    private Context context;
    private GoogleAccountCredential credential;
    private List<String> eventStrings;

    public GoogleCalendarManager(Context context) {
        this.context = context;
        this.eventStrings = new ArrayList<>();
        initializeCredential();
    }

    private void initializeCredential() {
        credential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    public void setSelectedAccount(Account account) {
        credential.setSelectedAccount(account);
<<<<<<< HEAD

        HttpTransport transport = new NetHttpTransport();
=======
        
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
>>>>>>> 0e723050db7ad92052d48a1d8f5f6e5e3e7cd9c2
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        
        mService = new Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("ToDo List App")
                .build();
    }

    public boolean isCredentialSet() {
        return credential != null && credential.getSelectedAccount() != null;
    }

    public GoogleAccountCredential getCredential() {
        return credential;
    }

    /*
     * 캘린더 ID를 가져오는 메소드
     */
    private String getCalendarID(String calendarTitle) throws IOException {
        if (mService == null) {
            return null;
        }

        CalendarList calendarList = mService.calendarList().list().execute();
        for (CalendarListEntry calendarListEntry : calendarList.getItems()) {
            if (calendarListEntry.getSummary().equals(calendarTitle)) {
                return calendarListEntry.getId();
            }
        }
        return null;
    }

    /*
     * 선택되어 있는 Google 계정에 새 캘린더를 추가한다.
     */
    public String createCalendar() throws IOException {
        if (mService == null) {
            return "Google 계정이 선택되지 않았습니다.";
        }

        String ids = getCalendarID("ToDo List Calendar");

        if (ids != null) {
            return "이미 캘린더가 생성되어 있습니다.";
        }

        // 새로운 캘린더 생성
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();

        // 캘린더의 제목 설정
        calendar.setSummary("ToDo List Calendar");
        calendar.setDescription("자동으로 생성된 ToDo 앱 캘린더");

        // 캘린더의 시간대 설정
        calendar.setTimeZone("Asia/Seoul");

        // 구글 캘린더에 새로 만든 캘린더를 추가
        com.google.api.services.calendar.model.Calendar createdCalendar = mService.calendars().insert(calendar).execute();

        // 추가한 캘린더의 ID를 가져옴.
        String calendarId = createdCalendar.getId();

        // 구글 캘린더의 캘린더 목록에서 새로 만든 캘린더를 검색
        CalendarListEntry calendarListEntry = mService.calendarList().get(calendarId).execute();

        // 캘린더의 배경색을 파란색으로 표시 RGB
        calendarListEntry.setBackgroundColor("#0000ff");

        // 변경한 내용을 구글 캘린더에 반영
        CalendarListEntry updatedCalendarListEntry =
                mService.calendarList()
                        .update(calendarListEntry.getId(), calendarListEntry)
                        .setColorRgbFormat(true)
                        .execute();

        // 새로 추가한 캘린더의 ID를 리턴
        return "캘린더가 생성되었습니다.";
    }

    /*
     * ToDo 항목을 Google Calendar 이벤트로 추가
     */
    public String addTodoToCalendar(Todo todo) throws IOException {
        if (mService == null) {
            return "Google 계정이 선택되지 않았습니다.";
        }

        String calendarId = getCalendarID("ToDo List Calendar");
        if (calendarId == null) {
            // 캘린더가 없으면 생성
            String createResult = createCalendar();
            if (!createResult.equals("캘린더가 생성되었습니다.")) {
                return createResult;
            }
            calendarId = getCalendarID("ToDo List Calendar");
        }

        Event event = new Event()
                .setSummary(todo.getTitle())
                .setDescription(todo.getDescription());

        // 마감일이 있는 경우
        if (todo.getDueDate() != null) {
            EventDateTime dueDateTime = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(todo.getDueDate()))
                    .setTimeZone("Asia/Seoul");
            event.setStart(dueDateTime);
            event.setEnd(dueDateTime);
        } else {
            // 마감일이 없으면 오늘 날짜로 설정
            Date today = new Date();
            EventDateTime todayDateTime = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(today))
                    .setTimeZone("Asia/Seoul");
            event.setStart(todayDateTime);
            event.setEnd(todayDateTime);
        }

        Event createdEvent = mService.events().insert(calendarId, event).execute();
        return "ToDo가 캘린더에 추가되었습니다: " + createdEvent.getHtmlLink();
    }

    /*
     * 완료된 ToDo 항목을 캘린더에서 삭제 (필요시)
     */
    public String removeTodoFromCalendar(Todo todo) throws IOException {
        // 구현 필요시 추가
        return "캘린더에서 삭제 기능은 아직 구현되지 않았습니다.";
    }

    /*
     * 테스트 이벤트를 캘린더에 추가
     */
    public String addEvent() throws IOException {
        String calendarID = getCalendarID("ToDo List Calendar");

        if (calendarID == null) {
            return "캘린더를 먼저 생성하세요.";
        }

        Event event = new Event()
                .setSummary("구글 캘린더 테스트")
                .setLocation("서울시")
                .setDescription("캘린더에 이벤트 추가하는 것을 테스트합니다.");

        java.util.Calendar calendar;

        calendar = java.util.Calendar.getInstance();
        SimpleDateFormat simpledateformat;
        // Z에 대응하여 +0900이 입력되어 문제 생겨 수작업으로 입력
        simpledateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+09:00", Locale.KOREA);
        String datetime = simpledateformat.format(calendar.getTime());

        DateTime startDateTime = new DateTime(datetime);
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Seoul");
        event.setStart(start);

        Log.d("@@@", datetime);

        DateTime endDateTime = new DateTime(datetime);
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Seoul");
        event.setEnd(end);

        try {
            event = mService.events().insert(calendarID, event).execute();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", "Exception : " + e.toString());
            return "이벤트 생성 실패: " + e.getMessage();
        }
        
        Log.e("Event", "created : " + event.getHtmlLink());
        String eventStrings = "created : " + event.getHtmlLink();
        return eventStrings;
    }

    /*
     * ToDo List Calendar 이름의 캘린더에서 10개의 이벤트를 가져와 리턴
     */
    public String getEvent() throws IOException {
        eventStrings.clear(); // 기존 데이터 클리어

        DateTime now = new DateTime(System.currentTimeMillis());

        String calendarID = getCalendarID("ToDo List Calendar");
        if (calendarID == null) {
            return "캘린더를 먼저 생성하세요.";
        }

        Events events = mService.events().list(calendarID)
                .setMaxResults(10)
                //.setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                // 모든 이벤트가 시작 시간을 갖고 있지는 않다. 그런 경우 시작 날짜만 사용
                start = event.getStart().getDate();
            }

            eventStrings.add(String.format("%s \n (%s)", event.getSummary(), start));
        }

        return eventStrings.size() + "개의 데이터를 가져왔습니다.";
    }

    /*
     * 가져온 이벤트 목록을 반환
     */
    public List<String> getEventStrings() {
        return new ArrayList<>(eventStrings);
    }
} 