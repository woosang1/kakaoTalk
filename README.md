# 💬 KakaoTalk Clone

Jetpack Compose 기반 카카오톡 스타일 채팅 앱입니다.

## ✨ 주요 기능
- 채팅방 목록 조회
- 실시간 채팅 메시지 송수신
- 카카오톡 스타일 UI/UX

## 🧱 아키텍처
Clean Architecture + MVVM
- Presentation (Compose UI + ViewModel)
- Domain (Business Logic)
- Data (Repository + Network/Database)

## 🧰 기술 스택
- Jetpack Compose
- Hilt (DI)
- Retrofit, OkHttp
- Coroutines + Flow
- Room
- Coil (이미지 로딩)
- Material 3

### 📦 디렉토리 구조
```
kakaoTalk/
├── app/                            // 애플리케이션 관련 설정 및 실행
│   └── src/main/java/
│       └── com/example/kakaotalk/
│           ├── MainActivity.kt     // 메인 액티비티
│           ├── model/             // 데이터 모델 (ChatRoom, ChatMessage)
│           └── ui/                 // UI 컴포넌트
│               ├── chat/           // 채팅 관련 화면
│               │   ├── ChatListScreen.kt
│               │   ├── ChatRoomScreen.kt
│               │   └── ChatViewModel.kt
│               └── theme/          // 테마 설정
│
├── app-config/                     // 앱 설정 모듈
│   ├── app-config/                 // 앱 설정 구현
│   └── app-config-api/             // 앱 설정 API
│
├── build-logic/                    // 빌드 로직 및 플러그인
│   └── src/main/kotlin/            // 커스텀 Gradle 플러그인
│
├── core/                           // 애플리케이션 전반에서 공통적으로 사용되는 core 모듈
│   ├── base/                       // 화면 등 기본 클래스
│   ├── database/                   // 데이터베이스 관련 코드 (Room 등)
│   ├── designsystem/               // 디자인 시스템 관련 코드 (컬러, 폰트, 스타일 등)
│   ├── model/                      // 애플리케이션에서 사용되는 모델 클래스
│   ├── navigation/                 // 네비게이션 관련 코드 (Jetpack Navigation 등)
│   ├── network/                    // 네트워크 관련 코드 (API 통신)
│   ├── resource/                   // 리소스 관련 코드 (예: 이미지, 문자열)
│   ├── testing/                    // 테스팅 관련 코드
│   ├── ui/                         // UI 관련 컴포넌트
│   └── utils/                      // 유틸리티 함수들 (DateFormatter, StringHelper 등)
│
└── gradle/                         // Gradle 설정
    ├── libs.versions.toml          // 의존성 버전 관리
    └── dependencyGraph.gradle      // 의존성 그래프 생성
```

## Screenshots
<p align="center">
  <img src="screenshots/chat_list.png" width="250" alt="Chat List">
  <img src="screenshots/chat_room.png" width="250" alt="Chat Room">
</p>

## 📦 의존 그래프
<p align="center">
  <img src="./project.dot.png" width="1000" alt="Dependency Graph">
</p>

