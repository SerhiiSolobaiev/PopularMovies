package com.myandroid.popularmovies.entities;

import android.graphics.Bitmap;

public class MovieItem {

    private int id;
    private Bitmap image;
    private String title;
    private String voteAverage;
    private String overview;
    private String releaseDate;
    private int isFavorite;

    public MovieItem(int id, Bitmap image, String title, String voteAverage,
                     String overview, String releaseDate, int isFavorite) {
        super();
        this.id = id;
        this.image = image;
        this.title = title;
        this.voteAverage = voteAverage;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.isFavorite = isFavorite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVote_average() {
        return voteAverage;
    }

    public void setVote_average(String vote_average) {
        this.voteAverage = vote_average;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return releaseDate;
    }

    public void setRelease_date(String release_date) {
        this.releaseDate = release_date;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }
}
