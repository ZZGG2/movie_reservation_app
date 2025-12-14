package com.cookandroid.movie_reservation_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    TextView tvProfileName, tvAccountName, imgAvatar;
    ImageButton btnEditProfile;
    Button btnResetDB;
    LinearLayout btnGoHistory, btnGoScrap; // ★ 스크랩 버튼 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvAccountName = findViewById(R.id.tvAccountName);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnResetDB = findViewById(R.id.btnResetDB);

        btnGoHistory = findViewById(R.id.btnGoHistory);
        btnGoScrap = findViewById(R.id.btnGoScrap); // ★ 연결

        updateProfileInfo();

        // 프로필 수정
        btnEditProfile.setOnClickListener(v -> {
            final EditText editText = new EditText(this);
            editText.setHint("Enter new nickname");
            new AlertDialog.Builder(this)
                    .setTitle("Change Nickname")
                    .setView(editText)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newNick = editText.getText().toString();
                        if (!newNick.isEmpty()) {
                            saveNickname(newNick);
                            updateProfileInfo();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // DB 초기화
        btnResetDB.setOnClickListener(v -> {
            DBHelper myHelper = new DBHelper(this);
            SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
            myHelper.onUpgrade(sqlDB, 1, 2);
            sqlDB.close();
            Toast.makeText(getApplicationContext(), "All data has been reset.", Toast.LENGTH_SHORT).show();
        });

        // 예매 내역 이동
        btnGoHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MyTicketListActivity.class);
            startActivity(intent);
        });

        // ★ 스크랩 목록 이동
        btnGoScrap.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MyScrapListActivity.class);
            startActivity(intent);
        });

        NavHelper.setupNavigation(this);
    }

    private void saveNickname(String nick) {
        SharedPreferences pref = getSharedPreferences("UserPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("nickname", nick);
        editor.apply();
    }

    private void updateProfileInfo() {
        SharedPreferences pref = getSharedPreferences("UserPref", MODE_PRIVATE);
        String nick = pref.getString("nickname", "John Doe");

        tvProfileName.setText(nick);
        tvAccountName.setText(nick);

        if (nick.length() >= 2) {
            imgAvatar.setText(nick.substring(0, 2).toUpperCase());
        } else {
            imgAvatar.setText(nick.toUpperCase());
        }
    }
}