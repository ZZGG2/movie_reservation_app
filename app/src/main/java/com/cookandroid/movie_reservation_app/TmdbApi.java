package com.cookandroid.movie_reservation_app;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TmdbApi {
    // 1. 현재 상영중인 영화 (홈 화면용)
    @GET("movie/now_playing")
    Call<MovieResponse> getNowPlayingMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    // ★ 2. 영화 검색 (게시판 글쓰기용 - 새로 추가됨)
    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("query") String query // 검색어
    );
}