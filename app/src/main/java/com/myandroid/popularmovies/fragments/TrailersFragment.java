package com.myandroid.popularmovies.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.myandroid.popularmovies.R;
import com.myandroid.popularmovies.Utility;
import com.myandroid.popularmovies.activities.DetailActivity;
import com.myandroid.popularmovies.adapters.ListTrailerAdapter;
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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class TrailersFragment extends Fragment{

    private final String LOG_TAG = TrailersFragment.class.getSimpleName();

    TextView tvTrailers;
    ListView listViewTrailers;
    ListTrailerAdapter adapter;

    private int idMovie;

    public TrailersFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            idMovie = intent.getIntExtra("idMovie", 0);
        }
        View rootView = inflater.inflate(R.layout.fragment_trailers, container,false);

        tvTrailers = (TextView)rootView.findViewById(R.id.textView_Trailers);
        listViewTrailers = (ListView)rootView.findViewById(R.id.listViewTrailers);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isNetworkConnected()) {
            (new GetTrailers()).execute(idMovie);
        }
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private class GetTrailers extends AsyncTask<Integer,Void,ArrayList<TrailerItem>> {

        private ArrayList<TrailerItem> getTrailersFromJSON(String JSONString){
            Log.v(LOG_TAG,"JSONString = "+JSONString);

            final String RESULTS_LIST = "results";
            final String NAME_KEY = "key";
            final String NAME_TRAILER = "name";

            ArrayList<TrailerItem> trailers = null;

            try {
                JSONObject objectTrailers = new JSONObject(JSONString);
                JSONArray trailersArray = objectTrailers.getJSONArray(RESULTS_LIST);

                trailers = new ArrayList<>();
                for (int i = 0; i < trailersArray.length(); i++){
                    JSONObject trailer = trailersArray.getJSONObject(i);

                    String key = trailer.getString(NAME_KEY);
                    String name = trailer.getString(NAME_TRAILER);

                    trailers.add(new TrailerItem(key,name));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return trailers;
        }

        @Override
        protected ArrayList<TrailerItem> doInBackground(Integer... params) {

            String trailersJSONStr = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            final String BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String ID_MOVIE = params[0].toString();
            final String API_KEY = MainActivityFragment.API_KEY;
            final String VIDEOS = "/videos?";

            try {
                String uri = BASE_URL + ID_MOVIE + VIDEOS + "api_key=" + API_KEY;
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
                trailersJSONStr = buffer.toString();
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
            return getTrailersFromJSON(trailersJSONStr);
        }

        @Override
        protected void onPostExecute(final ArrayList<TrailerItem> trailerItems) {
            super.onPostExecute(trailerItems);
            Log.v(LOG_TAG, "trailerItems.size() = " + trailerItems.size());
            if (trailerItems.size() != 0){
                tvTrailers.setVisibility(View.VISIBLE);
            }
            adapter = new ListTrailerAdapter(getActivity(), R.layout.list_trailers, trailerItems);
            listViewTrailers.setAdapter(adapter);
            Utility.setListViewHeightBasedOnChildren(listViewTrailers);

            listViewTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Uri uri = Uri.parse("http://www.youtube.com/watch?v="
                            + trailerItems.get(position).getReferenceOnYoutube());
                    Intent playOnYoutube = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(playOnYoutube);
                    Log.i("Video", "Video Playing....");
                }
            });
        }
    }
}
