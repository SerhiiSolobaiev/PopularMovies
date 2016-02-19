package com.myandroid.popularmovies.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myandroid.popularmovies.FavMoviesProvider;
import com.myandroid.popularmovies.R;
import com.myandroid.popularmovies.entities.TrailerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.myandroid.popularmovies.R.color.colorPrimary;

public class DetailFragment extends Fragment {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();

    Animation animTranslate;
    private int idMovie;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        animTranslate = AnimationUtils.loadAnimation(getContext(), R.anim.anim_translate_button);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_details_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_to_favorites) {
            Toast.makeText(getActivity(), "Must be function that adds this movie to favorites",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ImageView poster = (ImageView) rootView.findViewById(R.id.imageView);
        TextView textView_title = (TextView) rootView.findViewById(R.id.textView_title);
        TextView textView_overview = (TextView) rootView.findViewById(R.id.textView_overview);
        TextView textView_date = (TextView) rootView.findViewById(R.id.textView_date);
        TextView textView_rating = (TextView) rootView.findViewById(R.id.textView_rating);
        final Button btnCheck = (Button) rootView.findViewById(R.id.buttonCheck);


        Intent intent = getActivity().getIntent();
        if (intent != null) {

            idMovie = intent.getIntExtra("idMovie", 0);
            Bitmap bitmap = intent.getParcelableExtra("image");
            String title = intent.getStringExtra("title");
            String overview = intent.getStringExtra("overview");
            String vote_average = intent.getStringExtra("voteAverage");
            String release_date = intent.getStringExtra("releaseDate");
            int isFavorite = intent.getIntExtra("isFavorite", 0);

            Log.v(LOG_TAG, "isFavorite = " + isFavorite);

            textView_title.setText(title);
            textView_title.setTypeface(null, Typeface.BOLD_ITALIC);
            textView_overview.setText(overview);
            textView_date.setText(release_date);
            textView_rating.setText(vote_average + "/10");
            //textView_rating.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            poster.setImageBitmap(bitmap);

            if (isFavorite == 1) {
                btnCheck.setBackgroundResource(R.drawable.qwe);
                btnCheck.setText("");
            }
        }

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.v(LOG_TAG, "pressed on add_to_favorites");
                v.startAnimation(animTranslate);
                if (btnCheck.getText() == getResources().getString(R.string.add_to_favorites)) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            btnCheck.setBackgroundResource(R.drawable.qwe);
                            btnCheck.setText("");
                            updateMovieToFavorite(true);
                        }
                    }, 350);

                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            btnCheck.setBackgroundColor(btnCheck.getContext().getResources().getColor(R.color.colorPrimary));
                            btnCheck.setText(getResources().getString(R.string.add_to_favorites));
                            updateMovieToFavorite(false);
                        }
                    }, 350);

                }
            }
        });
        return rootView;
    }

    private void updateMovieToFavorite(boolean add) {
        ContentValues cv = new ContentValues();
        if (add) {
            Toast.makeText(getActivity(), "add to favorites", Toast.LENGTH_SHORT).show();
            cv.put(FavMoviesProvider.IS_FAVORITE, "1");
        } else {
            cv.put(FavMoviesProvider.IS_FAVORITE, "0");
            Toast.makeText(getActivity(), "remove from favorites", Toast.LENGTH_SHORT).show();
        }
        Uri uri = ContentUris.withAppendedId(FavMoviesProvider.MOVIE_CONTENT_URI, idMovie);
        getActivity().getContentResolver().update(uri, cv, null, null);
        Log.d(LOG_TAG, "updated to favorite movie with id = " + idMovie);
    }
}
