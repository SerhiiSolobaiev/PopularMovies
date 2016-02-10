package com.myandroid.popularmovies.entities;

public class TrailerItem {
    private String referenceOnYoutube;
    private String nameTrailer;

    public TrailerItem(String referenceOnYoutube, String nameTrailer) {
        this.referenceOnYoutube = referenceOnYoutube;
        this.nameTrailer = nameTrailer;
    }

    public String getReferenceOnYoutube() {
        return referenceOnYoutube;
    }

    public void setReferenceOnYoutube(String referenceOnYoutube) {
        this.referenceOnYoutube = referenceOnYoutube;
    }

    public String getNameTrailer() {
        return nameTrailer;
    }

    public void setNameTrailer(String nameTrailer) {
        this.nameTrailer = nameTrailer;
    }
}
