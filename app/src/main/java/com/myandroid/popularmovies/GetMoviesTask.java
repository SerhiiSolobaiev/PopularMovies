package com.myandroid.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.myandroid.popularmovies.entities.MovieItem;
import com.myandroid.popularmovies.fragments.MainActivityFragment;
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

public class GetMoviesTask extends AsyncTask<String[], Void, ArrayList<MovieItem>> {

    private final String LOG_TAG = GetMoviesTask.class.getSimpleName();

    private ArrayList<MovieItem> arrayImages;
    private Context context;
    private AsyncTaskCompleteListener listener;
    private ArrayList<Integer> favoriteMoviesId;

    public GetMoviesTask(Context context, AsyncTaskCompleteListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public interface AsyncTaskCompleteListener {
        void onTaskComplete(ArrayList<MovieItem> movieItems);
    }

    private ArrayList<MovieItem> getMoviesFromJSON(String JSONString) {

        Log.v(LOG_TAG, "JSONString = " + JSONString);

        final String RESULTS_LIST = "results";
        final String NAME_ID = "id";
        final String NAME_IMAGE = "poster_path";
        final String NAME_MOVIE = "title";
        final String NAME_VOTE_AVERAGE = "vote_average";
        final String NAME_OVERVIEW = "overview";
        final String NAME_RELEASE_DATE = "release_date";

        try {

            JSONObject objectMovies = new JSONObject(JSONString);
            JSONArray moviesArray = objectMovies.getJSONArray(RESULTS_LIST);

            arrayImages = new ArrayList<>();
            favoriteMoviesId = readFavoriteMoviesFromBD();
            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movie = moviesArray.getJSONObject(i);

                int id = movie.getInt(NAME_ID);
                String imageName = movie.getString(NAME_IMAGE);
                String movieName = movie.getString(NAME_MOVIE);
                String vote_count = movie.getString(NAME_VOTE_AVERAGE);
                String overview = movie.getString(NAME_OVERVIEW);
                String release_date = movie.getString(NAME_RELEASE_DATE);
                int isFavorite = (favoriteMoviesId.contains(id)) ? 1 : 0;

                Log.v(LOG_TAG, i + " movieName = " + movieName);
                Bitmap bitmap = makeBitmapFromName(imageName);

                MovieItem item = new MovieItem(id, bitmap, movieName, vote_count, overview,
                        release_date,isFavorite);

                writeMoviesToBD(item);

                arrayImages.add(item);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayImages;
    }

    private Bitmap makeBitmapFromName(String imageName) {
        Bitmap bitmap = null;
        final String BASE_URL = "http://image.tmdb.org/t/p/w185/";
        try {
            bitmap = Picasso.with(context).load(BASE_URL + imageName).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void writeMoviesToBD(MovieItem item) {
        ContentValues values = new ContentValues();
        values.put(FavMoviesProvider.ID_MOVIE, item.getId());
        values.put(FavMoviesProvider.TITLE, item.getTitle());
        values.put(FavMoviesProvider.IMAGE, Utility.getBytes(item.getImage()));
        values.put(FavMoviesProvider.OVERVIEW, item.getOverview());
        values.put(FavMoviesProvider.VOTE_AVERAGE, item.getVote_average());
        values.put(FavMoviesProvider.RELEASE_DATE, item.getRelease_date());

        if (!favoriteMoviesId.contains(item.getId())) {
            values.put(FavMoviesProvider.IS_FAVORITE, 0);
            Uri uri = context.getContentResolver().insert(
                    FavMoviesProvider.MOVIE_CONTENT_URI, values);
            Log.d(LOG_TAG, "insert, result Uri = " + uri.toString());
        }
        else{
            values.put(FavMoviesProvider.IS_FAVORITE, 1);
            Uri uri = ContentUris.withAppendedId(FavMoviesProvider.MOVIE_CONTENT_URI, item.getId());
            context.getContentResolver().update(uri, values, null, null);
            Log.d(LOG_TAG, "favorite movie with id = " + item.getId());
        }
    }

    private ArrayList<Integer> readFavoriteMoviesFromBD(){
        favoriteMoviesId = new ArrayList<>();
        String where = FavMoviesProvider.IS_FAVORITE + " = 1";
        Cursor c = context.getContentResolver().query(
                FavMoviesProvider.MOVIE_CONTENT_URI, new String[]{FavMoviesProvider.ID_MOVIE},
                where, null, null);

        if (c.moveToFirst()) {
            do {
                favoriteMoviesId.add(c.getInt(c.getColumnIndex(FavMoviesProvider.ID_MOVIE)));
            } while (c.moveToNext());
        }
        c.close();
        return favoriteMoviesId;
    }

    @Override
    protected ArrayList<MovieItem> doInBackground(String[]... params) {

        String moviesJSONStr = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        final String BASE_URL = "https://api.themoviedb.org/3";
        final String PARAM = "/discover/movie?";
        String sortMethod = params[0][0]; //first parameter
        String page = params[0][1];

        try {
            String uri = BASE_URL + PARAM + "sort_by=" + sortMethod + "&page=" + page
                    + "&api_key=" + MainActivityFragment.API_KEY;
            Log.v(LOG_TAG, uri);
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
        return getMoviesFromJSON(moviesJSONStr);
    }


    @Override
    protected void onPostExecute(ArrayList<MovieItem> moviesArray) {
        super.onPostExecute(moviesArray);
        listener.onTaskComplete(moviesArray);
    }
}