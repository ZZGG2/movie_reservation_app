package com.cookandroid.movie_reservation_app;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast; // 토스트 메시지용

import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class MyScrapListActivity extends Activity {

    ListView listViewScrap;
    DBHelper myHelper;
    ImageView btnBack;
    ArrayList<ScrapData> scrapList = new ArrayList<>();
    ScrapAdapter adapter; // 어댑터를 전역변수로 (새로고침 위해)

    class ScrapData {
        String title, date, posterUrl;
        double rating;
        public ScrapData(String t, String d, double r, String u) {
            title = t; date = d; rating = r; posterUrl = u;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_scrap_list);

        listViewScrap = findViewById(R.id.listViewScrap);
        btnBack = findViewById(R.id.btnBack);

        myHelper = new DBHelper(this);

        loadScrapList();

        btnBack.setOnClickListener(v -> finish());

        NavHelper.setupNavigation(this);
    }

    private void loadScrapList() {
        SQLiteDatabase sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("SELECT * FROM scrapTBL;", null);

        scrapList.clear();
        while(cursor.moveToNext()) {
            // 0:title, 1:date, 2:rating, 3:posterUrl
            scrapList.add(new ScrapData(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    cursor.getString(3)));
        }
        cursor.close();
        sqlDB.close();

        adapter = new ScrapAdapter(this, scrapList);
        listViewScrap.setAdapter(adapter);
    }

    class ScrapAdapter extends BaseAdapter {
        Context context;
        ArrayList<ScrapData> list;

        public ScrapAdapter(Context c, ArrayList<ScrapData> l) { context = c; list = l; }
        public int getCount() { return list.size(); }
        public Object getItem(int i) { return list.get(i); }
        public long getItemId(int i) { return i; }

        public View getView(int i, View view, ViewGroup g) {
            if(view == null) view = LayoutInflater.from(context).inflate(R.layout.item_scrap, g, false);

            TextView t = view.findViewById(R.id.tvScrapTitle);
            TextView d = view.findViewById(R.id.tvScrapDate);
            TextView r = view.findViewById(R.id.tvScrapRating);
            ImageView iv = view.findViewById(R.id.ivScrapPoster);

            // ★ 삭제 버튼 연결
            ImageButton btnRemove = view.findViewById(R.id.btnRemoveScrap);

            ScrapData data = list.get(i);

            t.setText(data.title);
            d.setText(data.date);
            r.setText("★ " + data.rating);
            Glide.with(context).load(data.posterUrl).into(iv);

            // ★ 삭제 버튼 클릭 이벤트
            btnRemove.setOnClickListener(v -> {
                // 1. DB에서 삭제
                SQLiteDatabase db = myHelper.getWritableDatabase();
                db.execSQL("DELETE FROM scrapTBL WHERE title = ?", new String[]{data.title});
                db.close();

                // 2. 리스트(메모리)에서 삭제
                list.remove(i);

                // 3. 화면 새로고침
                notifyDataSetChanged();

                Toast.makeText(context, "Removed from Scraps.", Toast.LENGTH_SHORT).show();
            });

            return view;
        }
    }
}