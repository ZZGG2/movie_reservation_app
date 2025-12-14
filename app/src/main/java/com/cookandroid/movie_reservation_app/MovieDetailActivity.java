package com.cookandroid.movie_reservation_app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class MovieDetailActivity extends Activity {

    String title, date, posterUrl, backdropUrl, overview;
    double rating;
    boolean isScrapped = false; // 스크랩 여부

    DBHelper myHelper;
    SQLiteDatabase sqlDB;
    ImageButton btnScrap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Window window = getWindow();
        if (window != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
            window.setLayout(width, height);
        }

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        date = intent.getStringExtra("date");
        posterUrl = intent.getStringExtra("poster");
        backdropUrl = intent.getStringExtra("backdrop");
        overview = intent.getStringExtra("overview");
        rating = intent.getDoubleExtra("rating", 0.0);

        myHelper = new DBHelper(this);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvRating = findViewById(R.id.tvRating);
        TextView tvOverview = findViewById(R.id.tvOverview);
        ImageView ivPoster = findViewById(R.id.ivPoster);
        ImageView ivBackdrop = findViewById(R.id.ivBackdrop);

        Button btnBookNow = findViewById(R.id.btnBookNow);
        Button btnGoReview = findViewById(R.id.btnGoReview); // 리뷰 버튼
        ImageButton btnClose = findViewById(R.id.btnClose);
        btnScrap = findViewById(R.id.btnScrap); // 스크랩 버튼

        tvTitle.setText(title);
        tvDate.setText(date);
        tvRating.setText(String.valueOf(rating));
        if (overview == null || overview.isEmpty()) tvOverview.setText("No overview available.");
        else tvOverview.setText(overview);

        if (posterUrl != null) Glide.with(this).load(posterUrl).into(ivPoster);
        if (backdropUrl != null) Glide.with(this).load(backdropUrl).into(ivBackdrop);

        // 1. 스크랩 상태 확인
        checkScrapStatus();

        // 2. 스크랩 버튼 클릭
        btnScrap.setOnClickListener(v -> {
            if (isScrapped) {
                // 이미 스크랩됨 -> 삭제
                sqlDB = myHelper.getWritableDatabase();
                sqlDB.execSQL("DELETE FROM scrapTBL WHERE title = ?", new String[]{title});
                sqlDB.close();
                isScrapped = false;
                Toast.makeText(this, "Removed from Scraps", Toast.LENGTH_SHORT).show();
            } else {
                // 스크랩 안됨 -> 저장
                sqlDB = myHelper.getWritableDatabase();
                sqlDB.execSQL("INSERT OR IGNORE INTO scrapTBL VALUES (?, ?, ?, ?);",
                        new Object[]{title, date, rating, posterUrl});
                sqlDB.close();
                isScrapped = true;
                Toast.makeText(this, "Saved to Scraps!", Toast.LENGTH_SHORT).show();
            }
            updateScrapIcon();
        });

        // 3. 리뷰 작성 버튼 클릭 -> BoardActivity로 이동 (제목 전달)
        btnGoReview.setOnClickListener(v -> {
            Intent reviewIntent = new Intent(getApplicationContext(), BoardActivity.class);
            reviewIntent.putExtra("targetMovie", title); // 영화 제목을 넘겨줌 (자동완성에 쓰려고)
            startActivity(reviewIntent);
        });

        btnBookNow.setOnClickListener(v -> {
            Intent bookIntent = new Intent(getApplicationContext(), BookingActivity.class);
            bookIntent.putExtra("title", title);
            startActivity(bookIntent);
        });

        btnClose.setOnClickListener(v -> finish());
    }

    private void checkScrapStatus() {
        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("SELECT * FROM scrapTBL WHERE title = ?", new String[]{title});
        if (cursor.getCount() > 0) isScrapped = true;
        else isScrapped = false;
        cursor.close();
        sqlDB.close();
        updateScrapIcon();
    }

    private void updateScrapIcon() {
        if (isScrapped) {
            btnScrap.setColorFilter(Color.parseColor("#E50914")); // 빨간색 (저장됨)
        } else {
            btnScrap.setColorFilter(Color.parseColor("#888888")); // 회색 (안됨)
        }
    }
}