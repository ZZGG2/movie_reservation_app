package com.cookandroid.movie_reservation_app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookingActivity extends FragmentActivity implements OnMapReadyCallback {

    // ★ API 키 입력 필수
    private static final String API_KEY = BuildConfig.TMDB_API_KEY;
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private GoogleMap mMap;

    LinearLayout movieContainer, layoutTheaterSeat, seatContainer, layoutDateTime;
    RadioGroup rgDate, rgTime;
    TableLayout tableSeats;
    Button btnConfirm;
    TextView tvSeatInfo, tvTotalPrice, tvSelectedTheater;

    ArrayList<String> selectedSeatList = new ArrayList<>();
    String selectedMovieTitle = "";
    String selectedPosterUrl = "";
    String selectedTheaterName = "";

    View selectedMovieView = null;
    int pricePerTicket = 14000;

    DBHelper myHelper;
    SQLiteDatabase sqlDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        movieContainer = findViewById(R.id.movieContainer);
        layoutTheaterSeat = findViewById(R.id.layoutTheaterSeat);
        seatContainer = findViewById(R.id.seatContainer);
        layoutDateTime = findViewById(R.id.layoutDateTime);
        tvSelectedTheater = findViewById(R.id.tvSelectedTheater);

        rgDate = findViewById(R.id.rgDate);
        rgTime = findViewById(R.id.rgTime);
        tableSeats = findViewById(R.id.tableSeats);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvSeatInfo = findViewById(R.id.tvSeatInfo);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        myHelper = new DBHelper(this);

        // 1. 영화 목록 API 호출
        fetchMoviesFromApi();

        // 2. 동적 날짜 생성
        setDynamicDates();

        // 3. 지도 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 4. 시간 선택 리스너 (좌석표 갱신)
        rgTime.setOnCheckedChangeListener((group, checkedId) -> {
            if (rgDate.getCheckedRadioButtonId() != -1 && checkedId != -1) {
                if (seatContainer.getVisibility() == View.GONE) {
                    seatContainer.setVisibility(View.VISIBLE);
                }
                refreshSeatTable(); // 예약된 좌석 확인 후 그리기
            }
        });

        // 날짜 변경 시에도 좌석 갱신
        rgDate.setOnCheckedChangeListener((group, checkedId) -> {
            if (rgTime.getCheckedRadioButtonId() != -1) {
                refreshSeatTable();
            }
        });

        // 5. 예약 버튼
        btnConfirm.setOnClickListener(v -> {
            if(selectedMovieTitle.equals("")) {
                Toast.makeText(this, "Select a movie.", Toast.LENGTH_SHORT).show(); return;
            }
            if(selectedTheaterName.equals("")) {
                Toast.makeText(this, "Select a theater.", Toast.LENGTH_SHORT).show(); return;
            }
            if(rgDate.getCheckedRadioButtonId() == -1 || rgTime.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Select date/time.", Toast.LENGTH_SHORT).show(); return;
            }
            if(selectedSeatList.isEmpty()) {
                Toast.makeText(this, "Select seats.", Toast.LENGTH_SHORT).show(); return;
            }

            // 날짜 정보 가져오기 (Tag 사용)
            int dateId = rgDate.getCheckedRadioButtonId();
            String date = ((RadioButton)findViewById(dateId)).getTag().toString();

            int timeId = rgTime.getCheckedRadioButtonId();
            String time = ((RadioButton)findViewById(timeId)).getText().toString();

            StringBuilder seats = new StringBuilder();
            for(String s : selectedSeatList) seats.append(s).append(" ");

            try {
                sqlDB = myHelper.getWritableDatabase();
                String sql = "INSERT INTO bookingTBL (title, date, time, seat, theater, posterUrl) VALUES (?, ?, ?, ?, ?, ?)";
                Object[] args = {selectedMovieTitle, date, time, seats.toString(), selectedTheaterName, selectedPosterUrl};
                sqlDB.execSQL(sql, args);
                sqlDB.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "DB Error", Toast.LENGTH_SHORT).show(); return;
            }

            Toast.makeText(getApplicationContext(), "Booking Confirmed!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), TicketActivity.class);
            intent.putExtra("title", selectedMovieTitle);
            intent.putExtra("posterUrl", selectedPosterUrl);
            intent.putExtra("date", date);
            intent.putExtra("time", time);
            intent.putExtra("seat", seats.toString());
            intent.putExtra("theater", selectedTheaterName);
            startActivity(intent);
            finish();
        });

        NavHelper.setupNavigation(this);
    }

    // ★ 동적 날짜 생성 함수
    private void setDynamicDates() {
        rgDate.removeAllViews();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE\ndd", Locale.ENGLISH);
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            RadioButton rdbtn = new RadioButton(this);
            rdbtn.setId(View.generateViewId());
            rdbtn.setText(dateFormat.format(calendar.getTime()));
            rdbtn.setTag(dbFormat.format(calendar.getTime())); // DB 저장용 실제 날짜

            rdbtn.setBackgroundResource(R.drawable.selector_time);
            rdbtn.setButtonDrawable(null);
            rdbtn.setTextColor(Color.WHITE);
            rdbtn.setGravity(Gravity.CENTER);

            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(width, height);
            params.setMargins(0, 0, margin, 0);
            rdbtn.setLayoutParams(params);

            rgDate.addView(rdbtn);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    // DB에서 이미 예약된 좌석 조회
    private ArrayList<String> getBookedSeats(String date, String time, String theater) {
        ArrayList<String> bookedSeats = new ArrayList<>();
        sqlDB = myHelper.getReadableDatabase();
        String query = "SELECT seat FROM bookingTBL WHERE date=? AND time=? AND theater=?";
        Cursor cursor = sqlDB.rawQuery(query, new String[]{date, time, theater});

        while (cursor.moveToNext()) {
            String seatString = cursor.getString(0);
            String[] seats = seatString.split(" ");
            for (String s : seats) {
                if (!s.trim().isEmpty()) bookedSeats.add(s.trim());
            }
        }
        cursor.close();
        sqlDB.close();
        return bookedSeats;
    }

    // 좌석표 새로고침
    private void refreshSeatTable() {
        int dateId = rgDate.getCheckedRadioButtonId();
        int timeId = rgTime.getCheckedRadioButtonId();

        if (dateId == -1 || timeId == -1) return;

        String date = ((RadioButton)findViewById(dateId)).getTag().toString();
        String time = ((RadioButton)findViewById(timeId)).getText().toString();

        ArrayList<String> bookedSeats = getBookedSeats(date, time, selectedTheaterName);

        selectedSeatList.clear();
        updatePrice();
        createCinemaSeats(bookedSeats);
    }

    // 좌석 생성 (중복 처리 포함)
    private void createCinemaSeats(ArrayList<String> bookedSeats) {
        tableSeats.removeAllViews();
        for (int i = 0; i < 8; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            tableRow.setGravity(Gravity.CENTER);
            char rowChar = (char) ('A' + i);

            for (int j = 1; j <= 8; j++) {
                ToggleButton btn = new ToggleButton(this);
                String seatName = rowChar + String.valueOf(j);
                btn.setText(seatName); btn.setTextOn(seatName); btn.setTextOff(seatName);
                btn.setTextSize(10);

                if (bookedSeats.contains(seatName)) {
                    btn.setEnabled(false);
                    btn.setBackgroundColor(Color.parseColor("#444444")); // Occupied
                    btn.setTextColor(Color.parseColor("#888888"));
                } else {
                    btn.setEnabled(true);
                    btn.setBackgroundResource(R.drawable.selector_seat);
                    btn.setTextColor(Color.WHITE);
                    btn.setOnClickListener(v -> {
                        if (btn.isChecked()) selectedSeatList.add(seatName);
                        else selectedSeatList.remove(seatName);
                        updatePrice();
                    });
                }

                int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());
                int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
                TableRow.LayoutParams params = new TableRow.LayoutParams(size, size);
                if (j == 4) params.setMargins(margin, margin, margin * 6, margin);
                else params.setMargins(margin, margin, margin, margin);
                btn.setLayoutParams(params);
                tableRow.addView(btn);
            }
            tableSeats.addView(tableRow);
        }
    }

    // 초기 좌석 생성 (빈 리스트)
    private void createCinemaSeats() {
        createCinemaSeats(new ArrayList<>());
    }

    // ... (onMapReady, fetchMoviesFromApi 등 나머지 메소드는 위와 동일) ...
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng centerPoint = new LatLng(37.2221, 127.1880);
        LatLng engBldg5 = new LatLng(37.221980, 127.187623);
        LatLng myongjin = new LatLng(37.222163, 127.188526);
        LatLng hambak = new LatLng(37.221144, 127.188625);
        LatLng engBldg1 = new LatLng(37.222496, 127.186922);
        LatLng changjo = new LatLng(37.222859, 127.189251);

        mMap.addMarker(new MarkerOptions().position(engBldg5).title("5공학관 (CineMax 1관)"));
        mMap.addMarker(new MarkerOptions().position(myongjin).title("명진당 (CineMax 2관)"));
        mMap.addMarker(new MarkerOptions().position(hambak).title("함박관 (CineMax 3관)"));
        mMap.addMarker(new MarkerOptions().position(engBldg1).title("1공학관 (CineMax 4관)"));
        mMap.addMarker(new MarkerOptions().position(changjo).title("창조관 (CineMax 5관)"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, 17));

        mMap.setOnMarkerClickListener(marker -> {
            selectedTheaterName = marker.getTitle();
            tvSelectedTheater.setText("Selected: " + selectedTheaterName);
            tvSelectedTheater.setTextColor(Color.parseColor("#E50914"));
            if(layoutDateTime.getVisibility() == View.GONE) layoutDateTime.setVisibility(View.VISIBLE);
            marker.showInfoWindow();
            return true;
        });
    }

    private void fetchMoviesFromApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        TmdbApi api = retrofit.create(TmdbApi.class);

        api.getNowPlayingMovies(API_KEY, "ko-KR", 1).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateMovieScroll(response.body().getResults());
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {}
        });
    }

    private void updateMovieScroll(List<Movie> movieList) {
        movieContainer.removeAllViews();
        String intentTitle = getIntent().getStringExtra("title");

        for (Movie movie : movieList) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_booking_movie, movieContainer, false);

            ImageView ivPoster = itemView.findViewById(R.id.ivPoster);
            TextView tvTitle = itemView.findViewById(R.id.tvTitle);
            TextView tvDate = itemView.findViewById(R.id.tvDate);
            TextView tvRating = itemView.findViewById(R.id.tvRating);

            tvTitle.setText(movie.getTitle());
            tvDate.setText(movie.getReleaseDate());
            tvRating.setText(String.valueOf(movie.getVoteAverage()));
            Glide.with(this).load(movie.getPosterPath()).into(ivPoster);

            itemView.setOnClickListener(v -> {
                if (selectedMovieView != null) selectedMovieView.setSelected(false);
                v.setSelected(true);
                selectedMovieView = v;

                selectedMovieTitle = movie.getTitle();
                selectedPosterUrl = movie.getPosterPath();

                layoutTheaterSeat.setVisibility(View.VISIBLE);
            });

            if(intentTitle != null && intentTitle.equals(movie.getTitle())) {
                itemView.performClick();
            }
            movieContainer.addView(itemView);
        }
    }

    private void updatePrice() {
        int count = selectedSeatList.size();
        int total = count * pricePerTicket;
        tvSeatInfo.setText("Selected (" + count + ")");
        tvTotalPrice.setText(total + "원");
    }
}