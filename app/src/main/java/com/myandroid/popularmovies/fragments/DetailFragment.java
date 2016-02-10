package com.myandroid.popularmovies.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class DetailFragment extends Fragment{

    private final String LOG_TAG = DetailFragment.class.getSimpleName();

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_details_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_to_favorites) {
            Toast.makeText(getActivity(),"Must be function that adds this movie to favorites",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ImageView poster = (ImageView)rootView.findViewById(R.id.imageView);
        TextView textView_title = (TextView)rootView.findViewById(R.id.textView_title);
        TextView textView_overview = (TextView)rootView.findViewById(R.id.textView_overview);
        TextView textView_date = (TextView)rootView.findViewById(R.id.textView_date);
        TextView textView_rating = (TextView)rootView.findViewById(R.id.textView_rating);


        Intent intent = getActivity().getIntent();
        if (intent != null) {

            Bitmap bitmap = intent.getParcelableExtra("image");
            String title = intent.getStringExtra("title");
            String overview = intent.getStringExtra("overview");
            String vote_average = intent.getStringExtra("vote_average");
            String release_date = intent.getStringExtra("release_date");

            textView_title.setText(title);
            //textView_title.setTypeface(null, Typeface.BOLD_ITALIC);
            textView_overview.setText(overview);
            textView_date.setText(release_date);
            textView_rating.setText(vote_average + "/10");
            //textView_rating.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            poster.setImageBitmap(bitmap);
        }

        final Button btnCheck = (Button)rootView.findViewById(R.id.buttonCheck);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnCheck.getText() == getResources().getString(R.string.add_to_favorites)){
                    btnCheck.setBackgroundResource(R.drawable.qwe);
                    btnCheck.setText("");
                }
                else {
                    btnCheck.setBackgroundColor(btnCheck.getContext().getResources().getColor(R.color.colorPrimary));
                    btnCheck.setText(getResources().getString(R.string.add_to_favorites));
                }
            }
        });
        return rootView;
    }
}
