package com.cookandroid.movie_reservation_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity {

    // ★ API 키 확인
    private static final String API_KEY = BuildConfig.TMDB_API_KEY;
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    GridLayout gridMovies;
    MyGalleryAdapter galAdapter;
    TextView tvViewAll, tvListTitle;
    EditText edtSearch;
    ImageButton btnSearch;

    TmdbApi api;
    List<Movie> allMovieList = new ArrayList<>(); // 하단 그리드용 데이터
    boolean isExpanded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 갤러리 연결
        Gallery gallery = findViewById(R.id.gallery1);
        galAdapter = new MyGalleryAdapter(this);
        gallery.setAdapter(galAdapter);

        // 2. 나머지 위젯 연결
        gridMovies = findViewById(R.id.gridMovies);
        tvViewAll = findViewById(R.id.tvViewAll);
        tvListTitle = findViewById(R.id.tvListTitle);
        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);

        // 3. Retrofit 설정
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(TmdbApi.class);

        // 4. 초기 데이터 로드
        loadNowPlayingMovies(true); // true: 갤러리도 같이 업데이트

        // --- 이벤트 리스너 ---

        gallery.setOnItemClickListener((parent, view, position, id) ->
                moveToDetail(galAdapter.getItem(position))
        );

        tvViewAll.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            updateGrid();
        });

        btnSearch.setOnClickListener(v -> performSearch());

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        findViewById(R.id.btnQuickBooking).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), BookingActivity.class));
        });
        findViewById(R.id.btnQuickCommunity).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), BoardActivity.class));
        });

        NavHelper.setupNavigation(this);
    }

    private void performSearch() {
        String query = edtSearch.getText().toString().trim();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);

        if (query.isEmpty()) {
            // 검색어 없으면 초기 상태로 (갤러리는 유지해도 되지만 여기선 그냥 둠)
            loadNowPlayingMovies(false); // false: 갤러리는 업데이트 안 함 (이미 있으니까)
        } else {
            searchMovies(query);
        }
    }

    // 현재 상영작 불러오기
    private void loadNowPlayingMovies(boolean updateGallery) {
        api.getNowPlayingMovies(API_KEY, "ko-KR", 1).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> list = response.body().getResults();

                    // 인기순 정렬
                    Collections.sort(list, new Comparator<Movie>() {
                        @Override
                        public int compare(Movie m1, Movie m2) {
                            return Double.compare(m2.getPopularity(), m1.getPopularity());
                        }
                    });

                    tvListTitle.setText("Now Showing");
                    allMovieList = list;

                    if (updateGallery) {
                        galAdapter.setMovies(list);
                    }

                    updateGrid();
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {}
        });
    }

    // 영화 검색하기
    private void searchMovies(String query) {
        api.searchMovies(API_KEY, "ko-KR", query).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> results = response.body().getResults();
                    if (results.isEmpty()) {
                        Toast.makeText(MainActivity.this, "No movies found.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 인기순 정렬 (선택 사항)
                        Collections.sort(results, new Comparator<Movie>() {
                            @Override
                            public int compare(Movie m1, Movie m2) {
                                return Double.compare(m2.getPopularity(), m1.getPopularity());
                            }
                        });

                        tvListTitle.setText("Search Results");
                        allMovieList = results;

                        // ★ 갤러리 업데이트 코드가 없으므로 하단만 바뀜!
                        updateGrid();
                    }
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {}
        });
    }

    private void updateGrid() {
        gridMovies.removeAllViews();
        int displayCount = isExpanded ? allMovieList.size() : Math.min(4, allMovieList.size());
        tvViewAll.setText(isExpanded ? "Show Less" : "View All");

        for (int i = 0; i < displayCount; i++) {
            Movie movie = allMovieList.get(i);
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_movie_card, gridMovies, false);

            ImageView ivPoster = cardView.findViewById(R.id.ivPoster);
            TextView tvTitle = cardView.findViewById(R.id.tvTitle);
            TextView tvDate = cardView.findViewById(R.id.tvDate);
            TextView tvRating = cardView.findViewById(R.id.tvRating);

            tvTitle.setText(movie.getTitle());
            tvDate.setText(movie.getReleaseDate());
            tvRating.setText("★ " + movie.getVoteAverage());
            Glide.with(this).load(movie.getPosterPath()).into(ivPoster);

            cardView.setOnClickListener(v -> moveToDetail(movie));
            gridMovies.addView(cardView);
        }
    }

    private void moveToDetail(Object item) {
        if (item instanceof Movie) {
            Movie movie = (Movie) item;
            Intent intent = new Intent(getApplicationContext(), MovieDetailActivity.class);
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("date", movie.getReleaseDate());
            intent.putExtra("rating", movie.getVoteAverage());
            intent.putExtra("overview", movie.getOverview());
            intent.putExtra("poster", movie.getPosterPath());
            intent.putExtra("backdrop", movie.getBackdropPath());
            startActivity(intent);
        }
    }
}