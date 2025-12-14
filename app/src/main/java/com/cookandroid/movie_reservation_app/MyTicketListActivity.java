package com.cookandroid.movie_reservation_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide; // Glide 임포트 확인

import java.util.ArrayList;

public class MyTicketListActivity extends Activity {

    ListView listViewHistory;
    DBHelper myHelper;
    SQLiteDatabase sqlDB;
    ImageView btnBack;

    class TicketData {
        String title, date, time, theater, seat, posterUrl;
        public TicketData(String t, String d, String tm, String th, String s, String url) {
            title = t; date = d; time = tm; theater = th; seat = s; posterUrl = url;
        }
    }
    ArrayList<TicketData> ticketList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ticket_list);

        listViewHistory = findViewById(R.id.listViewHistory);
        btnBack = findViewById(R.id.btnBack);

        myHelper = new DBHelper(this);
        loadTicketHistory();

        btnBack.setOnClickListener(v -> finish());

        // 리스트 클릭 시 상세 티켓으로 이동
        listViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TicketData data = ticketList.get(position);
                Intent intent = new Intent(getApplicationContext(), TicketActivity.class);
                intent.putExtra("title", data.title);
                intent.putExtra("date", data.date);
                intent.putExtra("time", data.time);
                intent.putExtra("seat", data.seat);
                intent.putExtra("theater", data.theater);
                intent.putExtra("posterUrl", data.posterUrl);
                startActivity(intent);
            }
        });

        NavHelper.setupNavigation(this);
    }

    private void loadTicketHistory() {
        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("SELECT * FROM bookingTBL ORDER BY id DESC;", null);

        ticketList.clear();
        while(cursor.moveToNext()) {
            String title = cursor.getString(1);
            String date = cursor.getString(2);
            String time = cursor.getString(3);
            String seat = cursor.getString(4);
            String theater = cursor.getString(5);
            String posterUrl = "";
            try { posterUrl = cursor.getString(6); } catch (Exception e) {}

            ticketList.add(new TicketData(title, date, time, theater, seat, posterUrl));
        }
        cursor.close();
        sqlDB.close();

        TicketAdapter adapter = new TicketAdapter(this, ticketList);
        listViewHistory.setAdapter(adapter);
    }

    public class TicketAdapter extends BaseAdapter {
        Context context;
        ArrayList<TicketData> list;

        public TicketAdapter(Context c, ArrayList<TicketData> l) { context = c; list = l; }
        public int getCount() { return list.size(); }
        public Object getItem(int i) { return list.get(i); }
        public long getItemId(int i) { return i; }

        public View getView(int i, View view, ViewGroup group) {
            if(view == null) view = LayoutInflater.from(context).inflate(R.layout.item_ticket_history, group, false);

            TextView tvTitle = view.findViewById(R.id.tvHistoryTitle);
            TextView tvDate = view.findViewById(R.id.tvHistoryDate);
            TextView tvTime = view.findViewById(R.id.tvHistoryTime);
            TextView tvTheater = view.findViewById(R.id.tvHistoryTheater);

            // ★ 수정된 부분: 아이디 찾기 및 이미지 로딩
            ImageView ivTinyPoster = view.findViewById(R.id.ivTinyPoster);

            TicketData data = list.get(i);
            tvTitle.setText(data.title);
            tvDate.setText(data.date);
            tvTime.setText(data.time);
            tvTheater.setText(data.theater + " | Seats: " + data.seat);

            // Glide로 작은 포스터 로딩
            if (data.posterUrl != null && !data.posterUrl.isEmpty()) {
                Glide.with(context).load(data.posterUrl).into(ivTinyPoster);
            } else {
                // 이미지가 없으면 기본 티켓 아이콘
                ivTinyPoster.setImageResource(R.drawable.ic_ticket);
            }

            return view;
        }
    }
}