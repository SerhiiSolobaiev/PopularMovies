package com.myandroid.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.myandroid.popularmovies.fragments.DetailFragment;
import com.myandroid.popularmovies.R;
import com.myandroid.popularmovies.fragments.TrailersFragment;

public class DetailActivity extends AppCompatActivity{

    //TODO
        /*
        1.Send data (id) of the film to TrailersFragment
        2.In TrailersFragment run AsyncTask, get name and URL trailers and put them in Linear layout
         */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //details fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
        //trailers fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.containerForTrailers, new TrailersFragment())
                    .commit();
        }
    }
}
