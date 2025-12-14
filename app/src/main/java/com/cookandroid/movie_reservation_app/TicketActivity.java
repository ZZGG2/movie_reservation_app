package com.cookandroid.movie_reservation_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Random;

public class TicketActivity extends Activity {

    String title = "", date = "", time = "", seat = "", theater = "";
    String posterUrl = "";

    boolean hasTicket = false;
    Bitmap posterBitmap = null;
    MyTicketView myTicketView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        LinearLayout layout = findViewById(R.id.ticketLayout);
        myTicketView = new MyTicketView(this);
        layout.addView(myTicketView);

        NavHelper.setupNavigation(this);
    }

    // ★ 핵심 수정: 화면이 뜰 때마다 DB를 새로 조회하도록 onResume에 로직 작성
    @Override
    protected void onResume() {
        super.onResume();

        // 1. Intent로 받은 데이터가 있으면 그걸 우선 사용
        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra("title") != null) {
            title = intent.getStringExtra("title");
            posterUrl = intent.getStringExtra("posterUrl");
            date = intent.getStringExtra("date");
            time = intent.getStringExtra("time");
            seat = intent.getStringExtra("seat");
            theater = intent.getStringExtra("theater");
            hasTicket = true;

            // Intent 데이터는 한 번 쓰고 지워주는 게 좋음 (다음에 DB 불러오기 위해)
            intent.removeExtra("title");
            loadPosterImage();
        }
        // 2. 없으면 무조건 DB에서 최신 내역 불러오기
        else {
            loadLastTicketFromDB();
        }
    }

    private void loadLastTicketFromDB() {
        DBHelper myHelper = new DBHelper(this);
        SQLiteDatabase sqlDB = myHelper.getReadableDatabase();

        // 가장 최근(ID가 제일 큰) 예매 내역 1개 가져오기
        Cursor cursor = sqlDB.rawQuery("SELECT * FROM bookingTBL ORDER BY id DESC LIMIT 1;", null);

        if (cursor.moveToNext()) {
            // bookingTBL 컬럼 순서: 0:id, 1:title, 2:date, 3:time, 4:seat, 5:theater, 6:posterUrl
            title = cursor.getString(1);
            date = cursor.getString(2);
            time = cursor.getString(3);
            seat = cursor.getString(4);
            theater = cursor.getString(5);

            // DB 버전이 낮아서 posterUrl 컬럼이 없을 수도 있으니 예외처리
            try {
                posterUrl = cursor.getString(6);
            } catch (Exception e) {
                posterUrl = ""; // URL 없음
            }
            hasTicket = true;

            // 데이터 다 가져왔으니 이미지 로딩 시작
            loadPosterImage();
        } else {
            title = "No Ticket";
            hasTicket = false;
            if(myTicketView != null) myTicketView.invalidate(); // 화면 갱신
        }
        cursor.close();
        sqlDB.close();
    }

    // 이미지 다운로드 함수 분리
    private void loadPosterImage() {
        if (hasTicket && posterUrl != null && !posterUrl.isEmpty()) {
            Glide.with(this)
                    .asBitmap()
                    .load(posterUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            posterBitmap = resource;
                            if(myTicketView != null) myTicketView.invalidate(); // 다 되면 다시 그리기
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) { }
                    });
        } else {
            // URL이 없으면 비트맵 초기화
            posterBitmap = null;
            if(myTicketView != null) myTicketView.invalidate();
        }
    }

    // 커스텀 뷰 (티켓 그리기)
    private class MyTicketView extends View {
        public MyTicketView(Context context) { super(context); }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint paint = new Paint();
            int w = getWidth();
            int h = getHeight();

            canvas.drawColor(Color.parseColor("#121212"));

            if (!hasTicket) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(50);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("No Reservation Found", w / 2, h / 2, paint);
                return;
            }

            // 티켓 카드
            paint.setColor(Color.parseColor("#1E1E1E"));
            RectF ticketRect = new RectF(50, 50, w - 50, h - 200);
            canvas.drawRoundRect(ticketRect, 40, 40, paint);

            // 텍스트
            paint.setColor(Color.WHITE);
            paint.setTextSize(70);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setFakeBoldText(true);
            canvas.drawText(title, 100, 180, paint);

            paint.setColor(Color.GRAY);
            paint.setTextSize(40);
            paint.setFakeBoldText(false);
            canvas.drawText("Movie Ticket", 100, 240, paint);

            // ★ 포스터 그리기
            if (posterBitmap != null) {
                Bitmap scaledBmp = Bitmap.createScaledBitmap(posterBitmap, 400, 600, true);
                canvas.drawBitmap(scaledBmp, w - 450, 100, null);
            } else {
                // 이미지가 없을 때 회색 박스
                paint.setColor(Color.DKGRAY);
                canvas.drawRect(w - 450, 100, w - 50, 700, paint);

                // 텍스트라도 표시
                paint.setColor(Color.WHITE);
                paint.setTextSize(30);
                canvas.drawText("No Image", w - 400, 400, paint);
            }

            // 상세 정보
            int startY = 400;
            paint.setColor(Color.GRAY);
            paint.setTextSize(35);
            canvas.drawText("DATE", 100, startY, paint);
            canvas.drawText("TIME", 100, startY + 150, paint);
            canvas.drawText("THEATER", 100, startY + 300, paint);
            canvas.drawText("SEATS", 100, startY + 450, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            paint.setFakeBoldText(true);

            if(date != null) canvas.drawText(date, 100, startY + 70, paint);
            if(time != null) canvas.drawText(time, 100, startY + 220, paint);
            if(theater != null) canvas.drawText(theater, 100, startY + 370, paint);
            if(seat != null) canvas.drawText(seat, 100, startY + 520, paint);

            // QR 코드
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            float qrSize = 300;
            float qrX = (w - qrSize) / 2;
            float qrY = h - 550;
            canvas.drawRoundRect(qrX, qrY, qrX + qrSize, qrY + qrSize, 20, 20, paint);

            paint.setColor(Color.BLACK);
            int cells = 9;
            float cellSize = qrSize / cells;
            java.util.Random random = new java.util.Random();

            for (int i = 0; i < cells; i++) {
                for (int j = 0; j < cells; j++) {
                    if (random.nextBoolean()) {
                        canvas.drawRect(qrX + i * cellSize, qrY + j * cellSize,
                                qrX + (i + 1) * cellSize, qrY + (j + 1) * cellSize, paint);
                    }
                }
            }
        }
    }
}