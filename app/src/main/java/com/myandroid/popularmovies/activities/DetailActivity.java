package com.myandroid.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.myandroid.popularmovies.fragments.DetailFragment;
import com.myandroid.popularmovies.R;
import com.myandroid.popularmovies.fragments.TrailersFragment;

public class DetailActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.add(R.id.container, new DetailFragment()); //details fragment
            transaction.add(R.id.containerForTrailers, new TrailersFragment());//trailers fragment

            transaction.commit();
        }
    }
}
