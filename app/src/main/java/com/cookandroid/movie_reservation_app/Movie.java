package com.cookandroid.movie_reservation_app;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("title")
    String title;

    @SerializedName("poster_path")
    String posterPath;

    @SerializedName("backdrop_path")
    String backdropPath;

    @SerializedName("vote_average")
    double voteAverage;

    @SerializedName("release_date")
    String releaseDate;

    @SerializedName("overview")
    String overview;

    @SerializedName("popularity")
    double popularity;

    public String getTitle() { return title; }
    public String getPosterPath() { return "https://image.tmdb.org/t/p/w500" + posterPath; }
    public String getBackdropPath() { return "https://image.tmdb.org/t/p/w780" + backdropPath; }
    public double getVoteAverage() { return voteAverage; }
    public String getReleaseDate() { return releaseDate; }
    public String getOverview() { return overview; }
    public double getPopularity() { return popularity; }
}