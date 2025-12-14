package com.cookandroid.movie_reservation_app;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieResponse {
    @SerializedName("results")
    List<Movie> results; // 영화 목록

    public List<Movie> getResults() { return results; }
}