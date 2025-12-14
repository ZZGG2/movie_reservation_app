package com.cookandroid.movie_reservation_app;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MyGalleryAdapter extends BaseAdapter {
    Context context;
    List<Movie> movies = new ArrayList<>();

    public MyGalleryAdapter(Context c) {
        context = c;
    }

    public void setMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    public int getCount() {
        return movies.size();
    }

    public Object getItem(int position) {
        return movies.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public String getTitle(int position) {
        return movies.get(position).getTitle();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gallery_poster, parent, false);

            // ★ 수정된 부분: dp -> px 변환 (화면 크기에 맞게 조절)
            // 너비 220dp, 높이 330dp 정도로 설정 (메인 갤러리 높이 450dp 안에 쏙 들어오게)
            int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 220, context.getResources().getDisplayMetrics());
            int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 330, context.getResources().getDisplayMetrics());

            convertView.setLayoutParams(new Gallery.LayoutParams(widthPx, heightPx));
        }

        ImageView imageView = convertView.findViewById(R.id.ivGalleryPoster);
        TextView tvRank = convertView.findViewById(R.id.tvRankBadge);

        Movie movie = movies.get(position);

        Glide.with(context)
                .load(movie.getPosterPath())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imageView);

        // 랭킹 배지 로직
        if (position == 0 || position == 1 || position == 2) {
            tvRank.setVisibility(View.VISIBLE);
            tvRank.setText(String.valueOf(position + 1));
        } else {
            tvRank.setVisibility(View.GONE);
        }

        return convertView;
    }
}