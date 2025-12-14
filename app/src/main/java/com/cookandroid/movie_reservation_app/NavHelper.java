package com.cookandroid.movie_reservation_app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;

public class NavHelper {

    public static void setupNavigation(final Activity activity) {
        Button btnHome = activity.findViewById(R.id.nav_home);
        Button btnBooking = activity.findViewById(R.id.nav_booking);
        Button btnTicket = activity.findViewById(R.id.nav_ticket);
        Button btnBoard = activity.findViewById(R.id.nav_board);
        Button btnSettings = activity.findViewById(R.id.nav_settings);

        // 현재 화면에 따라 버튼 색상 변경 (빨간색 강조)
        int selectedColor = Color.parseColor("#E50914");

        if (activity instanceof MainActivity) {
            updateBtnColor(btnHome, selectedColor);
        } else if (activity instanceof BookingActivity) {
            updateBtnColor(btnBooking, selectedColor);
        } else if (activity instanceof TicketActivity) {
            updateBtnColor(btnTicket, selectedColor);
        } else if (activity instanceof BoardActivity) {
            updateBtnColor(btnBoard, selectedColor);
        } else if (activity instanceof SettingsActivity) {
            updateBtnColor(btnSettings, selectedColor);
        }

        // 1. 홈으로 이동 (수정됨: SINGLE_TOP 추가)
        btnHome.setOnClickListener(v -> {
            if (!(activity instanceof MainActivity)) {
                Intent intent = new Intent(activity, MainActivity.class);
                // ★ 수정된 부분: CLEAR_TOP과 함께 SINGLE_TOP을 써야 화면이 안 깜빡거립니다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0); // 애니메이션 제거
            }
        });

        // 2. 예매하기
        btnBooking.setOnClickListener(v -> {
            if (!(activity instanceof BookingActivity)) {
                Intent intent = new Intent(activity, BookingActivity.class);
                // 예매 화면은 입력 데이터가 초기화되는 게 나으므로 플래그 없이 이동
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });

        // 3. 티켓
        btnTicket.setOnClickListener(v -> {
            if (!(activity instanceof MyTicketListActivity)) {
                Intent intent = new Intent(activity, MyTicketListActivity.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });

        // 4. 게시판
        btnBoard.setOnClickListener(v -> {
            if (!(activity instanceof BoardActivity)) {
                Intent intent = new Intent(activity, BoardActivity.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });

        // 5. 설정
        btnSettings.setOnClickListener(v -> {
            if (!(activity instanceof SettingsActivity)) {
                Intent intent = new Intent(activity, SettingsActivity.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });
    }

    // 버튼 색상 변경 함수
    private static void updateBtnColor(Button btn, int color) {
        btn.setTextColor(color);
        for (Drawable drawable : btn.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setTint(color);
            }
        }
    }
}