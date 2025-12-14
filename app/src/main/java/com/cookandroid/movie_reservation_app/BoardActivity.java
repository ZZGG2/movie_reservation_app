package com.cookandroid.movie_reservation_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet; // ★ 추가됨 (중복 제거용)
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BoardActivity extends Activity {

    // ★ API 키 확인
    private static final String API_KEY = BuildConfig.TMDB_API_KEY;
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    DBHelper myHelper;
    SQLiteDatabase sqlDB;

    ListView listViewReview;
    TextView tvReviewCount, tvAvgRating, tvMovieCount;
    ImageButton btnWriteFab;
    TmdbApi api;

    // 데이터 클래스
    class ReviewData {
        String id, movie, content, nick;
        float rating;
        int likes;
        public ReviewData(String id, String movie, float rating, String content, String nick, int likes) {
            this.id = id; this.movie = movie; this.rating = rating; this.content = content; this.nick = nick; this.likes = likes;
        }
    }
    ArrayList<ReviewData> reviewList = new ArrayList<>();
    ReviewAdapter mainAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        listViewReview = findViewById(R.id.listViewReview);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        tvMovieCount = findViewById(R.id.tvMovieCount);
        btnWriteFab = findViewById(R.id.btnWriteFab);

        myHelper = new DBHelper(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(TmdbApi.class);

        btnWriteFab.setOnClickListener(v -> showWriteDialog());

        listViewReview.setOnItemLongClickListener((parent, view, position, id) -> {
            ReviewData data = reviewList.get(position);
            sqlDB = myHelper.getWritableDatabase();
            sqlDB.execSQL("DELETE FROM reviewTBL WHERE id = " + data.id);
            sqlDB.close();
            Toast.makeText(getApplicationContext(), "Deleted!", Toast.LENGTH_SHORT).show();
            loadReviews();
            return true;
        });

        loadReviews();
        NavHelper.setupNavigation(this);
    }

    private void showWriteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.dialog_write_review, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        AutoCompleteTextView actvMovie = dialogView.findViewById(R.id.actvDialogMovie);
        ImageButton btnSearchMovie = dialogView.findViewById(R.id.btnSearchMovie);
        RatingBar rBar = dialogView.findViewById(R.id.rbDialogRating);
        EditText edtContent = dialogView.findViewById(R.id.edtDialogContent);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnSubmit = dialogView.findViewById(R.id.btnDialogSubmit);

        NoFilterAdapter autoAdapter = new NoFilterAdapter(this, android.R.layout.simple_dropdown_item_1line);
        actvMovie.setAdapter(autoAdapter);

        actvMovie.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                if (s.length() >= 1) {
                    api.searchMovies(API_KEY, "ko-KR", s.toString()).enqueue(new Callback<MovieResponse>() {
                        public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Movie> results = response.body().getResults();
                                List<String> titles = new ArrayList<>();
                                for (Movie m : results) titles.add(m.getTitle());
                                autoAdapter.setData(titles);
                                autoAdapter.notifyDataSetChanged();
                                if (!titles.isEmpty()) actvMovie.showDropDown();
                            }
                        }
                        public void onFailure(Call<MovieResponse> call, Throwable t) {}
                    });
                }
            }
        });

        btnSearchMovie.setOnClickListener(v -> {
            String query = actvMovie.getText().toString();
            if (!query.isEmpty()) {
                actvMovie.setText(actvMovie.getText());
                actvMovie.setSelection(actvMovie.getText().length());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(actvMovie.getWindowToken(), 0);
            }
        });

        actvMovie.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTitle = (String) parent.getItemAtPosition(position);
            actvMovie.setText(selectedTitle);
            actvMovie.setSelection(selectedTitle.length());
            actvMovie.dismissDropDown();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String movie = actvMovie.getText().toString();
            float rating = rBar.getRating();
            String content = edtContent.getText().toString();
            SharedPreferences pref = getSharedPreferences("UserPref", MODE_PRIVATE);
            String nick = pref.getString("nickname", "Anonymous");

            if(movie.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show(); return;
            }

            sqlDB = myHelper.getWritableDatabase();
            String saveContent = content + "||" + nick;
            sqlDB.execSQL("INSERT INTO reviewTBL (movieTitle, rating, content, likes) VALUES ('"
                    + movie + "', " + rating + ", '" + saveContent + "', 0);");
            sqlDB.close();

            Toast.makeText(getApplicationContext(), "Review Added!", Toast.LENGTH_SHORT).show();
            loadReviews();
            dialog.dismiss();
        });

        dialog.show();
    }

    // 답글 팝업
    private void showReplyDialog(String reviewId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dialog_reply, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        ListView lvReplies = view.findViewById(R.id.lvReplies);
        EditText edtReply = view.findViewById(R.id.edtReplyContent);
        Button btnSend = view.findViewById(R.id.btnSendReply);
        Button btnClose = view.findViewById(R.id.btnCloseReply);

        loadReplies(reviewId, lvReplies);

        btnSend.setOnClickListener(v -> {
            String replyContent = edtReply.getText().toString();
            if(replyContent.isEmpty()) return;

            SharedPreferences pref = getSharedPreferences("UserPref", MODE_PRIVATE);
            String nick = pref.getString("nickname", "Anonymous");

            sqlDB = myHelper.getWritableDatabase();
            sqlDB.execSQL("INSERT INTO replyTBL (reviewId, nickname, content) VALUES ("
                    + reviewId + ", '" + nick + "', '" + replyContent + "');");
            sqlDB.close();

            edtReply.setText("");
            loadReplies(reviewId, lvReplies);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void loadReplies(String reviewId, ListView listView) {
        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("SELECT * FROM replyTBL WHERE reviewId = " + reviewId, null);

        ArrayList<String> replies = new ArrayList<>();
        while(cursor.moveToNext()) {
            String nick = cursor.getString(2);
            String content = cursor.getString(3);
            replies.add(nick + ": " + content);
        }
        cursor.close();
        sqlDB.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, replies) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(android.graphics.Color.WHITE);
                return view;
            }
        };
        listView.setAdapter(adapter);
    }

    private void loadReviews() {
        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("SELECT * FROM reviewTBL ORDER BY id DESC;", null);

        reviewList.clear();
        float totalRating = 0;
        HashSet<String> uniqueMovies = new HashSet<>(); // ★ 중복 제거용 Set

        while(cursor.moveToNext()) {
            String id = cursor.getString(0);
            String movie = cursor.getString(1);
            float rating = cursor.getFloat(2);
            String rawContent = cursor.getString(3);
            int likes = 0;
            try { likes = cursor.getInt(4); } catch (Exception e) {}

            String content = rawContent;
            String nick = "Anonymous";
            if(rawContent.contains("||")) {
                String[] parts = rawContent.split("\\|\\|");
                content = parts[0];
                if(parts.length > 1) nick = parts[1];
            }

            reviewList.add(new ReviewData(id, movie, rating, content, nick, likes));
            totalRating += rating;

            // ★ 영화 제목을 Set에 추가 (중복된 제목은 자동으로 하나만 남음)
            uniqueMovies.add(movie);
        }

        // 통계 업데이트
        tvReviewCount.setText(String.valueOf(reviewList.size()));
        if(reviewList.size() > 0) {
            float avg = totalRating / reviewList.size();
            tvAvgRating.setText(String.format("%.1f", avg));
        } else {
            tvAvgRating.setText("0.0");
        }

        // ★ Movies 카운트 업데이트 (Set의 크기 = 영화 종류 수)
        tvMovieCount.setText(String.valueOf(uniqueMovies.size()));

        mainAdapter = new ReviewAdapter(this, reviewList);
        listViewReview.setAdapter(mainAdapter);

        cursor.close();
        sqlDB.close();
    }

    public class ReviewAdapter extends BaseAdapter {
        Context context;
        ArrayList<ReviewData> list;

        // 내 폰에 저장된 좋아요 기록을 불러오기 위한 설정
        SharedPreferences pref;

        public ReviewAdapter(Context c, ArrayList<ReviewData> l) {
            context = c;
            list = l;
            pref = context.getSharedPreferences("UserPref", Context.MODE_PRIVATE); // Pref 초기화
        }

        public int getCount() { return list.size(); }
        public Object getItem(int i) { return list.get(i); }
        public long getItemId(int i) { return i; }

        public View getView(int i, View view, ViewGroup group) {
            if(view == null) view = LayoutInflater.from(context).inflate(R.layout.item_review, group, false);

            TextView tvNick = view.findViewById(R.id.tvNick);
            TextView tvMovie = view.findViewById(R.id.tvMovieTitle);
            TextView tvContent = view.findViewById(R.id.tvContent);
            RatingBar rb = view.findViewById(R.id.itemRatingBar);

            LinearLayout btnLike = view.findViewById(R.id.btnLike);
            TextView tvLikeText = (TextView) btnLike.getChildAt(0); // "♡ Like" 텍스트뷰
            TextView tvLikeCount = view.findViewById(R.id.tvLikeCount);

            TextView btnReply = view.findViewById(R.id.btnReply);

            ReviewData data = list.get(i);
            tvNick.setText(data.nick);
            tvMovie.setText(data.movie);
            tvContent.setText(data.content);
            rb.setRating(data.rating);
            tvLikeCount.setText(String.valueOf(data.likes));

            // ★ 1. 현재 상태 확인 (내가 이 글에 좋아요를 눌렀나?)
            String key = "liked_" + data.id; // 예: liked_1, liked_5
            boolean isLiked = pref.getBoolean(key, false);

            // ★ 2. 상태에 따라 색상 변경 (눌렀으면 빨강, 아니면 회색)
            if (isLiked) {
                tvLikeText.setText("♥ Liked");
                tvLikeText.setTextColor(android.graphics.Color.parseColor("#E50914")); // 빨강
            } else {
                tvLikeText.setText("♡ Like");
                tvLikeText.setTextColor(android.graphics.Color.parseColor("#888888")); // 회색
            }

            // ★ 3. 좋아요 클릭 이벤트 (토글 기능)
            btnLike.setOnClickListener(v -> {
                boolean currentStatus = pref.getBoolean(key, false); // 현재 상태 다시 확인
                SharedPreferences.Editor editor = pref.edit();
                sqlDB = myHelper.getWritableDatabase();

                if (currentStatus) {
                    // [취소] 이미 눌러져 있으면 -> 1 감소
                    sqlDB.execSQL("UPDATE reviewTBL SET likes = likes - 1 WHERE id = " + data.id);
                    data.likes -= 1;
                    if (data.likes < 0) data.likes = 0; // 0보다 작아지지 않게 방지

                    editor.putBoolean(key, false); // 상태: 안 누름

                    // UI 즉시 반영
                    tvLikeText.setText("♡ Like");
                    tvLikeText.setTextColor(android.graphics.Color.parseColor("#888888"));
                } else {
                    // [좋아요] 안 눌러져 있으면 -> 1 증가
                    sqlDB.execSQL("UPDATE reviewTBL SET likes = likes + 1 WHERE id = " + data.id);
                    data.likes += 1;

                    editor.putBoolean(key, true); // 상태: 누름

                    // UI 즉시 반영
                    tvLikeText.setText("♥ Liked");
                    tvLikeText.setTextColor(android.graphics.Color.parseColor("#E50914"));
                }

                sqlDB.close();
                editor.apply(); // Pref 저장
                tvLikeCount.setText(String.valueOf(data.likes)); // 숫자 갱신
            });

            btnReply.setOnClickListener(v -> showReplyDialog(data.id));

            return view;
        }
    }

    public class NoFilterAdapter extends ArrayAdapter<String> {
        private List<String> items;
        public NoFilterAdapter(Context context, int resource) { super(context, resource); items = new ArrayList<>(); }
        public void setData(List<String> list) { items.clear(); items.addAll(list); }
        @Override public int getCount() { return items.size(); }
        @Override public String getItem(int position) { return items.get(position); }
        @Override public Filter getFilter() {
            return new Filter() {
                @Override protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults(); results.values = items; results.count = items.size(); return results;
                }
                @Override protected void publishResults(CharSequence constraint, FilterResults results) { notifyDataSetChanged(); }
            };
        }
    }
}