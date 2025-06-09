# 📱 ToDo List with Google Calendar Integration

안드로이드 ToDo 리스트 앱에 Google Calendar API를 통합한 프로젝트입니다.

## ✨ 주요 기능

### 📋 기본 ToDo 기능
- ✅ 할일 추가/수정/삭제
- ✅ 할일 완료/미완료 토글
- ✅ 마감일 설정 (DatePicker)
- ✅ 할일 상세보기
- ✅ 로컬 데이터 저장 (SharedPreferences + Gson)

### 📅 Google Calendar 연동
- 🔗 Google 계정 연결
- 📆 전용 캘린더 생성 ("ToDo List Calendar")
- 🔄 ToDo → Calendar 이벤트 동기화
- 🧪 테스트 이벤트 추가
- 👀 캘린더 이벤트 목록 조회

## 🛠️ 기술 스택

- **Language**: Java
- **Platform**: Android (MinSDK 21, TargetSDK 35)
- **UI**: Material Design, RecyclerView, CardView
- **Data**: SharedPreferences, Gson
- **API**: Google Calendar API v3
- **Auth**: Google Account Credential

## 📦 주요 라이브러리

```gradle
// UI Components
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'com.google.android.material:material:1.12.0'

// Data
implementation 'com.google.code.gson:gson:2.10.1'

// Google Calendar API
implementation 'com.google.api-client:google-api-client:2.0.1'
implementation 'com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0'
implementation 'com.google.auth:google-auth-library-oauth2-http:1.19.0'
implementation 'com.google.android.gms:play-services-auth:20.7.0'
```

## 🚀 설치 및 실행

### 1. 프로젝트 클론
```bash
git clone https://github.com/[YOUR_USERNAME]/To_do_list.git
cd To_do_list
```

### 2. Android Studio에서 열기
- Android Studio 실행
- "Open an existing project" 선택
- 클론한 폴더 선택

### 3. 의존성 동기화
- Gradle Sync 실행 (보통 자동으로 실행됨)

### 4. 실행
- 에뮬레이터 또는 실제 기기에서 실행
- Google 계정이 기기에 등록되어 있어야 함

## 📱 사용 방법

### 기본 설정
1. **앱 실행** → 기본 ToDo 화면 확인
2. **⋮ 메뉴** → "Google 계정 연결" → 계정 선택 및 권한 승인
3. **⋮ 메뉴** → "캘린더 생성" → ToDo 전용 캘린더 생성

### 주요 기능 사용
- **+ 버튼**: 새 할일 추가
- **체크박스**: 할일 완료/미완료 토글
- **할일 클릭**: 상세보기/편집
- **🗑️ 버튼**: 할일 삭제
- **⋮ 메뉴**: Google Calendar 관련 기능

### Google Calendar 연동
1. **캘린더 동기화**: 모든 ToDo를 캘린더 이벤트로 변환
2. **테스트 이벤트 추가**: 샘플 이벤트 추가
3. **이벤트 목록 보기**: 캘린더의 이벤트들 조회

## 🏗️ 프로젝트 구조

```
app/src/main/java/com/example/to_do_list/
├── MainActivity.java              # 메인 화면 (ToDo 리스트)
├── AddTodoActivity.java          # 할일 추가 화면
├── TodoDetailActivity.java       # 할일 상세/편집 화면
├── Todo.java                     # ToDo 데이터 모델
├── TodoAdapter.java              # RecyclerView 어댑터
├── TodoManager.java              # 로컬 데이터 관리
└── GoogleCalendarManager.java    # Google Calendar API 관리

app/src/main/res/
├── layout/
│   ├── activity_main.xml         # 메인 화면 레이아웃
│   ├── activity_add_todo.xml     # 할일 추가 레이아웃
│   └── item_todo.xml             # ToDo 항목 레이아웃
└── menu/
    └── main_menu.xml             # 메인 메뉴
```

## 🔧 주요 클래스 설명

### `GoogleCalendarManager`
- Google Calendar API 통합 관리
- 계정 인증, 캘린더 생성, 이벤트 CRUD

### `TodoManager`
- SharedPreferences + Gson을 사용한 로컬 데이터 관리
- Singleton 패턴으로 구현

### `TodoAdapter`
- RecyclerView를 위한 어댑터
- 할일 표시, 완료 토글, 삭제 기능

## ⚠️ 주의사항

- 인터넷 연결 필수 (Google Calendar API 사용)
- Google 계정이 기기에 등록되어 있어야 함
- Google Calendar API 권한 승인 필요

## 🛡️ 권한

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
```

## 📄 라이선스

이 프로젝트는 개인 학습/포트폴리오 목적으로 만들어졌습니다.

## 🤝 기여

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 문의

프로젝트에 대한 질문이나 제안사항이 있으시면 이슈를 생성해주세요.

---

**개발환경**: Android Studio | **타겟**: Android 5.0+ (API 21+) | **언어**: 한국어 