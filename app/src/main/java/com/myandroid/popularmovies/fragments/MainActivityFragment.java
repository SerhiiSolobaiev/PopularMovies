package com.myandroid.popularmovies.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.myandroid.popularmovies.adapters.GridImageViewAdapter;
import com.myandroid.popularmovies.entities.ImageItem;
import com.myandroid.popularmovies.R;
import com.myandroid.popularmovies.activities.DetailActivity;
import com.squareup.picasso.Picasso;

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

public class MainActivityFragment extends Fragment {

    //TODO
    /*
    * 1. Movie details
    * 2. Save instance state
    * 3. Add SQLite Database (save all data in DB and refresh if Internet is available)
    * 4. Make possibility to add movie to favorite list (and than show this movies)
    * 5. Something more...
    * */


    private final String LOG_TAG = "MainActivityFragment";
    public static final String API_KEY = "be4ec1e43fd93463f31b377680769c29";
    //private String sortMethod = "popularity.desc";

    private GridView gridView;
    private GridImageViewAdapter gridAdapter;
    private ProgressBar mProgressBar;

    AlertDialog levelDialog;
    int positionInAlertDialog = 0; //bad solution to declare it here(

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_sort_by) {
            setSortMethod();
            return true;
        }
        if (id == R.id.action_refresh) {
            if (positionInAlertDialog == 0) {
                runBackgroundThread("popularity.desc");
            }
            else {
                runBackgroundThread("vote_average.desc");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView) rootView.findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);;
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), DetailActivity.class);

                intent.putExtra("id", item.getId());
                intent.putExtra("image", item.getImage());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("vote_average", item.getVote_average());
                intent.putExtra("overview", item.getOverview());
                intent.putExtra("release_date", item.getRelease_date());

                startActivity(intent);
            }
        });
        if (savedInstanceState != null){
            positionInAlertDialog = savedInstanceState.getInt("positionInAlertDialog");
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            positionInAlertDialog = savedInstanceState.getInt("positionInAlertDialog");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("positionInAlertDialog", positionInAlertDialog);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (positionInAlertDialog == 0)
            runBackgroundThread("popularity.desc");
        else
            runBackgroundThread("vote_average.desc");
    }
    private void runBackgroundThread(String params){
        if (isNetworkConnected()) {
            if (gridView.isShown()) {
                gridView.setVisibility(View.INVISIBLE);
            }
            GetMoviesTask task = new GetMoviesTask();
            task.execute(params);
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else
            Toast.makeText(getActivity(),"Please, check your Internet connection",Toast.LENGTH_LONG)
                    .show();
    }
    private void setSortMethod(){
        Log.v(LOG_TAG,"in setSortMethod");
        final CharSequence[] items = {getResources().getString(R.string.sort_most_popular),
                getResources().getString(R.string.sort_highest_rated)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sort_order);
        builder.setSingleChoiceItems(items, positionInAlertDialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        positionInAlertDialog = 0;
                        runBackgroundThread("popularity.desc");// deprecated
                        break;
                    case 1:
                        positionInAlertDialog = 1;
                        runBackgroundThread("vote_average.desc");
                        break;
                }
                levelDialog.dismiss();
            }
        });
        levelDialog = builder.create();
        levelDialog.show();

    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private class GetMoviesTask extends AsyncTask<String, Void, ArrayList<ImageItem>> {

        private ArrayList<ImageItem> getImagesFromJSON(String JSONString){

            Log.v(LOG_TAG,"JSONString = "+JSONString);

            final String RESULTS_LIST = "results";
            final String NAME_ID = "id";
            final String NAME_IMAGE = "poster_path";
            final String NAME_MOVIE = "title";
            final String NAME_VOTE_AVERAGE = "vote_average";
            final String NAME_OVERVIEW = "overview";
            final String NAME_RELEASE_DATE = "release_date";

            ArrayList<ImageItem> arrayImages = null;
            try {

                JSONObject objectMovies = new JSONObject(JSONString);
                JSONArray moviesArray = objectMovies.getJSONArray(RESULTS_LIST);

                arrayImages = new ArrayList<>();
                for (int i = 0; i < moviesArray.length(); i++){
                    JSONObject movie = moviesArray.getJSONObject(i);

                    int id = movie.getInt(NAME_ID);
                    String imageName = movie.getString(NAME_IMAGE);
                    String movieName = movie.getString(NAME_MOVIE);
                    String vote_count = movie.getString(NAME_VOTE_AVERAGE);
                    String overview = movie.getString(NAME_OVERVIEW);
                    String release_date = movie.getString(NAME_RELEASE_DATE);

                    Log.v(LOG_TAG,i+" movieName = " + movieName);
                    Log.v(LOG_TAG,i+" vote_count = " + vote_count);
                    Bitmap bitmap = makeBitmapFromName(imageName);
                    arrayImages.add(new ImageItem(id, bitmap, movieName, vote_count,
                            overview, release_date));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return arrayImages;
        }

        private Bitmap makeBitmapFromName(String imageName){
            Bitmap bitmap = null;
            final String BASE_URL = "http://image.tmdb.org/t/p/w342/";
            try {
                bitmap = Picasso.with(getActivity()).load(BASE_URL+imageName).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected ArrayList<ImageItem> doInBackground(String... params) {

            String moviesJSONStr = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            final String BASE_URL = "https://api.themoviedb.org/3";
            final String PARAM = "/discover/movie?";
            String sortMethod = params[0];
            try {
                String uri = BASE_URL + PARAM + "sort_by=" + sortMethod + "&api_key=" + API_KEY;
                Log.v(LOG_TAG,uri);
                URL url = new URL(uri);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                moviesJSONStr = buffer.toString();
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error ", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return getImagesFromJSON(moviesJSONStr);
        }


        @Override
        protected void onPostExecute(ArrayList imageItemArray ) {
            if (!gridView.isShown()) {
                gridView.setVisibility(View.VISIBLE);
            }
            mProgressBar.setVisibility(View.GONE);
            gridAdapter = new GridImageViewAdapter(getActivity(), R.layout.grid_item_movie,
                    imageItemArray);
            gridView.setAdapter(gridAdapter);
        }
    }
}
