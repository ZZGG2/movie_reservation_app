# 🎬 CineMax (시네맥스)
> **위치 기반 스마트 영화 예매 & 커뮤니티 안드로이드 앱** > **Location-Based Smart Movie Reservation Platform**

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-007396?style=flat-square&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white)
![Google Maps](https://img.shields.io/badge/Google_Maps-4285F4?style=flat-square&logo=google-maps&logoColor=white)
![TMDB API](https://img.shields.io/badge/TMDB_API-01B4E4?style=flat-square&logo=themoviedb&logoColor=white)

## 📝 프로젝트 소개 (Project Overview)
**CineMax**는 실시간 영화 데이터를 기반으로 사용자의 위치(캠퍼스 건물)를 활용해 영화를 예매하고, 티켓을 발권하며, 리뷰를 공유할 수 있는 안드로이드 네이티브 앱입니다.

단순한 예매 기능을 넘어 **Google Maps API**를 활용한 직관적인 극장 선택, **Canvas API**를 이용한 커스텀 티켓 드로잉, **중복 예매 방지 알고리즘** 등 심화된 모바일 프로그래밍 기술이 적용되었습니다.

## 🛠 기술 스택 (Tech Stack)

| 구분 | 기술 / 라이브러리 | 상세 내용 |
| :--- | :--- | :--- |
| **Language** | Java | Android Native App Development |
| **Network** | Retrofit2, Gson | TMDB REST API 통신 및 JSON 파싱 |
| **LBS** | Google Maps SDK | 지도 표시, 커스텀 마커, 위치 기반 극장 선택 |
| **Image** | Glide | 고해상도 포스터 이미지 비동기 로딩 및 캐싱 |
| **Database** | SQLite | 로컬 데이터 영구 저장 (예매, 리뷰, 스크랩 등) |
| **Graphics** | Canvas API | 커스텀 E-Ticket 드로잉 및 QR 패턴 생성 |
| **UI** | Material Design | CardView, FloatingActionButton, Custom Dialog |

## ✨ 주요 기능 (Key Features)

### 1. 실시간 영화 정보 (Home & Search)
- **TMDB API 연동:** `Retrofit2`를 사용하여 실시간 상영작 정보를 불러옵니다.
- **인기순 정렬 & 랭킹:** API 데이터를 인기(Popularity) 순으로 정렬하고, TOP 3 영화에 랭킹 배지를 부여합니다.
- **검색 기능:** 검색어 입력 시 실시간으로 API를 호출하여 영화를 찾아줍니다.
- **View All:** 초기 4개 항목만 노출하고 '더 보기'를 통해 리스트를 확장/축소하는 동적 UI를 구현했습니다.

### 2. 위치 기반 원스톱 예매 시스템 (Booking)
- **Flow:** `영화 선택` → `극장 선택(지도)` → `날짜/시간 선택` → `좌석 선택`이 한 화면에서 이루어지는 UX.
- **Google Maps 연동:** 텍스트 목록 대신 지도를 통해 캠퍼스 내 건물(공학관, 도서관 등)을 극장으로 선택합니다.
- **동적 날짜 생성:** `Calendar` 객체를 활용해 오늘 날짜부터 7일간의 스케줄을 자동 생성합니다.
- **중복 예매 방지:** `SQLite`를 조회하여 선택한 날짜/시간에 이미 예약된 좌석은 비활성화(Disable) 처리합니다.

### 3. 커스텀 티켓 발권 (E-Ticket)
- **Canvas Drawing:** XML 레이아웃이 아닌 `onDraw()` 메소드를 오버라이딩하여 티켓 이미지와 절취선을 직접 그립니다.
- **Random QR:** `Random` 클래스를 활용해 티켓마다 고유한 패턴의 QR 코드를 생성하여 시각화했습니다.

### 4. 커뮤니티 & 마이페이지 (Community & My Page)
- **리뷰 게시판:** 영화별 리뷰 작성, 별점 부여, 좋아요(Toggle 기능), 대댓글 기능을 제공합니다.
- **스크랩(찜하기):** 상세 화면에서 영화를 스크랩하면 `scrapTBL`에 저장되어 모아볼 수 있습니다.
- **설정:** 프로필 수정 및 DB 초기화 기능을 제공합니다.

## 📱 실행 화면 (Screenshots)

| 홈 화면 (Home) | 예매 - 지도 (Map) | 예매 - 좌석 (Seat) | 티켓 (Ticket) |
| :---: | :---: | :---: | :---: |
| <img src="이미지경로/home.png" width="200"/> | <img src="이미지경로/map.png" width="200"/> | <img src="이미지경로/seat.png" width="200"/> | <img src="이미지경로/ticket.png" width="200"/> |

*(스크린샷 이미지를 프로젝트 폴더에 넣고 경로를 수정해주세요)*

## 💾 데이터베이스 설계 (Database Schema)

앱은 `SQLite` 내장 데이터베이스를 사용하여 총 4개의 테이블을 관리합니다.

1.  **bookingTBL**: 예매 내역 저장 (영화명, 날짜, 시간, 좌석, 극장, 포스터)
2.  **reviewTBL**: 리뷰 데이터 (영화명, 평점, 내용, 좋아요 수)
3.  **replyTBL**: 리뷰에 대한 대댓글
4.  **scrapTBL**: 스크랩(찜)한 영화 목록

## 🚀 설치 및 실행 (How to Run)

1.  프로젝트를 클론합니다.
    ```bash
    git clone [https://github.com/YOUR_ID/CineMax.git](https://github.com/YOUR_ID/CineMax.git)
    ```
2.  Android Studio에서 프로젝트를 엽니다 (`File > Open`).
3.  **API Key 설정이 필요합니다.**
    - `MainActivity.java`, `BookingActivity.java` 등의 `API_KEY` 변수에 본인의 TMDB API Key를 입력하세요.
    - `AndroidManifest.xml`의 `com.google.android.geo.API_KEY`에 본인의 Google Maps API Key를 입력하세요.
4.  에뮬레이터(API 34 권장) 또는 실제 기기에서 실행합니다.
    - *Note:* 에뮬레이터에서 지도 렌더링 문제 발생 시 `config.ini`의 `hw.gpu.mode`를 `angle_indirect`로 설정하세요.

## 👨‍💻 개발자 (Author)
- **Name:** [본인 이름]
- **Role:** Android Developer (Individual Project)
- **Contact:** [본인 이메일]

---
© 2024 CineMax Project. All Rights Reserved.
