package com.cookandroid.movie_reservation_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "movieAppDB", null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. 예매 테이블
        db.execSQL("CREATE TABLE bookingTBL (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title CHAR(50), date CHAR(20), time CHAR(20), seat CHAR(20), theater CHAR(20), posterUrl CHAR(200));");

        // 2. 리뷰 테이블
        db.execSQL("CREATE TABLE reviewTBL (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movieTitle CHAR(50), rating FLOAT, content CHAR(100), likes INTEGER DEFAULT 0);");

        // 3. 댓글 테이블
        db.execSQL("CREATE TABLE replyTBL (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reviewId INTEGER, nickname CHAR(20), content CHAR(100));");

        // 4. 스크랩 테이블
        db.execSQL("CREATE TABLE scrapTBL (" +
                "title CHAR(50) PRIMARY KEY, " +
                "date CHAR(20), " +
                "rating DOUBLE, " +
                "posterUrl CHAR(200));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bookingTBL");
        db.execSQL("DROP TABLE IF EXISTS reviewTBL");
        db.execSQL("DROP TABLE IF EXISTS replyTBL");
        db.execSQL("DROP TABLE IF EXISTS scrapTBL");
        onCreate(db);
    }
}