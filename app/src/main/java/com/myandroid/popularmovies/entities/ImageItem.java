package com.myandroid.popularmovies.entities;

import android.graphics.Bitmap;

public class ImageItem {
    private int id;
    private Bitmap image;
    private String title;
    private String vote_average;
    private String overview;
    private String release_date;

    public ImageItem(int id, Bitmap image, String title, String vote_average,
                     String overview, String release_date) {
        super();
        this.id = id;
        this.image = image;
        this.title = title;
        this.vote_average = vote_average;
        this.overview = overview;
        this.release_date = release_date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVote_average() {
        return vote_average;
    }

    public void setVote_average(String vote_average) {
        this.vote_average = vote_average;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
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
}
