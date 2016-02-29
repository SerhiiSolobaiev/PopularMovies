package com.myandroid.popularmovies.fragments;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.myandroid.popularmovies.FavMoviesProvider;
import com.myandroid.popularmovies.GetMoviesTask;
import com.myandroid.popularmovies.Utility;
import com.myandroid.popularmovies.adapters.GridImageViewAdapter;
import com.myandroid.popularmovies.entities.MovieItem;
import com.myandroid.popularmovies.R;
import com.myandroid.popularmovies.activities.DetailActivity;

import java.util.ArrayList;

public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    public static final String API_KEY = "be4ec1e43fd93463f31b377680769c29";

    private static final String APP_PREFERENCES = "preferences";
    private static final String APP_PREFERENCES_SORT_METHOD = "sort_method";
    private static final String APP_PREFERENCES_POSITION_IN_MENU = "position_in_menu";
    private SharedPreferences.Editor editor;

    private GridView gridView;
    private GridImageViewAdapter gridAdapter;
    private ProgressBar mProgressBar;
    private AlertDialog levelDialog;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private TextView textViewNoResults;
    private SharedPreferences sharedpreferences;

    private static Parcelable state;

    private ArrayList<MovieItem> arrayMovies;
    private int current_page = 1;
    private boolean loadMore = false;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sharedpreferences = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        arrayMovies = new ArrayList<>();
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
            String sortMethod = sharedpreferences
                    .getString(APP_PREFERENCES_SORT_METHOD, "popularity.desc");
            runBackgroundThread(sortMethod, current_page);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        gridView = (GridView) rootView.findViewById(android.R.id.list);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mySwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        textViewNoResults = (TextView) rootView.findViewById(R.id.textView_noResults);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieItem item = (MovieItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), DetailActivity.class);

                intent.putExtra("idMovie", item.getId());
                intent.putExtra("image", item.getImage());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("voteAverage", item.getVote_average());
                intent.putExtra("overview", item.getOverview());
                intent.putExtra("releaseDate", item.getRelease_date());
                intent.putExtra("isFavorite", item.getIsFavorite());

                startActivity(intent);
            }
        });

        setOnGridScrollListener();
        setOnSwypeRefreshGrid();

        if (state != null) {
            gridView.onRestoreInstanceState(state);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        readMoviesFromBD(sharedpreferences.getString(APP_PREFERENCES_SORT_METHOD,
                "popularity.desc"));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (state != null) {
            gridView.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onPause() {
        state = gridView.onSaveInstanceState();
        super.onPause();
    }

    private void setOnGridScrollListener() {
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((lastInScreen == totalItemCount) && loadMore && isNetworkConnected()) {
                    loadMore = false;
                    Log.v(LOG_TAG, "reached the end of the gridView");
                    runBackgroundThread(
                            sharedpreferences.getString(APP_PREFERENCES_SORT_METHOD,
                                    "popularity.desc"), current_page);
                }
            }
        });
    }

    private void setOnSwypeRefreshGrid() {
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        current_page = 1; //refresh all
                        runBackgroundThread(
                                sharedpreferences.getString(APP_PREFERENCES_SORT_METHOD,
                                        "popularity.desc"), current_page);
                    }
                }
        );
    }

    private void runBackgroundThread(String sortMethod, int page) {
        if (isNetworkConnected()) {
            if (!sharedpreferences.getString(
                    APP_PREFERENCES_SORT_METHOD, "popularity.desc").equals("isFavorite")) {

                Log.v(LOG_TAG, "Downloading starts ");
                Log.v(LOG_TAG, "sortMethod=" + sortMethod + ", page=" + page);

                String[] params = {sortMethod, String.valueOf(page)};

                if (page == 1) {
                    deleteOldMoviesFromBD(arrayMovies);
                }

                //start AsyncTask
                (new GetMoviesTask(getActivity(), new FetchMoviesTaskCompleteListener()))
                        .execute(params);

                textViewNoResults.setVisibility(View.GONE);
                if (!mySwipeRefreshLayout.isRefreshing()) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
            else
                mySwipeRefreshLayout.setRefreshing(false);

        } else {
            Toast.makeText(getActivity(), "Please, check your Internet connection",
                    Toast.LENGTH_LONG).show();
            mySwipeRefreshLayout.setRefreshing(false);

            //read movies that already saved in BD
            readMoviesFromBD(sharedpreferences.getString(APP_PREFERENCES_SORT_METHOD,
                    "popularity.desc"));

        }
    }

    private void readMoviesFromBD(String params) {
        String where = FavMoviesProvider.IS_FAVORITE + " = 0 OR "
                + FavMoviesProvider.IS_FAVORITE + " = 1";
        if (params.equals("isFavorite")) {
            where = FavMoviesProvider.IS_FAVORITE + " = 1";
        }

        Log.v(LOG_TAG, "\nreadMoviesFromBD(), where = " + where);

        Cursor c = getActivity().getContentResolver().query(
                FavMoviesProvider.MOVIE_CONTENT_URI, null, where, null, null);

        if (c.moveToFirst()) {
            do {
                MovieItem item = new MovieItem(
                        c.getInt(c.getColumnIndex(FavMoviesProvider.ID_MOVIE)),
                        Utility.getImage(c.getBlob(c.getColumnIndex(FavMoviesProvider.IMAGE))),
                        c.getString(c.getColumnIndex(FavMoviesProvider.TITLE)),
                        c.getString(c.getColumnIndex(FavMoviesProvider.VOTE_AVERAGE)),
                        c.getString(c.getColumnIndex(FavMoviesProvider.OVERVIEW)),
                        c.getString(c.getColumnIndex(FavMoviesProvider.RELEASE_DATE)),
                        c.getInt(c.getColumnIndex(FavMoviesProvider.IS_FAVORITE)));
                arrayMovies.add(item);
                Log.v(LOG_TAG, "item.getTitle() = " + item.getTitle());
                Log.v(LOG_TAG, "item.getIsFavorite() = " + item.getIsFavorite());
            } while (c.moveToNext());
        }
        c.close();

        gridAdapter = new GridImageViewAdapter(getActivity(), R.layout.grid_item_movie, arrayMovies);
        gridView.setAdapter(gridAdapter);
        loadMore = true;
        if (arrayMovies.size() == 0) {
            textViewNoResults.setVisibility(View.VISIBLE);
        }
    }

    private void deleteOldMoviesFromBD(ArrayList<MovieItem> arrayMovies) {
        if (arrayMovies.size() != 0) {
            for (int i = 0; i < arrayMovies.size(); i++) {
                if (arrayMovies.get(i).getIsFavorite() == 0) {
                    Uri uri = ContentUris.withAppendedId(FavMoviesProvider.MOVIE_CONTENT_URI,
                            arrayMovies.get(i).getId());
                    getActivity().getContentResolver().delete(uri, null, null);
                    Log.v(LOG_TAG, "deletedMovieId = " + arrayMovies.get(i).getId());
                } else {
                    Log.v(LOG_TAG, "favorite movie with id = "
                            + arrayMovies.get(i).getId() + " not deleted");
                }
            }
        }
    }

    private void setSortMethod() {
        Log.v(LOG_TAG, "in setSortMethod");
        editor = sharedpreferences.edit();
        current_page = 1; //for creating new query
        final CharSequence[] items = {getResources().getString(R.string.sort_most_popular),
                getResources().getString(R.string.sort_highest_rated),
                getResources().getString(R.string.sort_favorite)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sort_order);
        builder.setSingleChoiceItems(items,
                sharedpreferences.getInt(APP_PREFERENCES_POSITION_IN_MENU, 0), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        loadMore = false;
                        gridAdapter.clear();
                        switch (item) {
                            case 0:
                                editor.putInt(APP_PREFERENCES_POSITION_IN_MENU, 0);
                                editor.putString(APP_PREFERENCES_SORT_METHOD, "popularity.desc");
                                editor.apply();
                                runBackgroundThread(
                                        sharedpreferences.getString(APP_PREFERENCES_SORT_METHOD,
                                                "popularity.desc"), current_page);
                                break;
                            case 1:
                                editor.putInt(APP_PREFERENCES_POSITION_IN_MENU, 1);
                                editor.putString(APP_PREFERENCES_SORT_METHOD, "vote_count.desc");
                                editor.apply();
                                runBackgroundThread(
                                        sharedpreferences.getString(APP_PREFERENCES_SORT_METHOD,
                                                "popularity.desc"), current_page);
                                break;
                            case 2:
                                editor.putInt(APP_PREFERENCES_POSITION_IN_MENU, 2);
                                editor.putString(APP_PREFERENCES_SORT_METHOD, "isFavorite");
                                editor.apply();
                                readMoviesFromBD(sharedpreferences.getString(
                                        APP_PREFERENCES_SORT_METHOD, "popularity.desc"));
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

    public class FetchMoviesTaskCompleteListener implements GetMoviesTask.AsyncTaskCompleteListener {

        //background process finished
        @Override
        public void onTaskComplete(ArrayList<MovieItem> movieItems) {
            mProgressBar.setVisibility(View.GONE);
            mySwipeRefreshLayout.setRefreshing(false);

            int currentPosition = gridView.getFirstVisiblePosition();

            if (current_page == 1) {
                gridAdapter = new GridImageViewAdapter(getActivity(), R.layout.grid_item_movie,
                        movieItems);
            } else {
                gridAdapter.addAll(movieItems);
            }
            gridView.setSelection(currentPosition + 1);
            gridView.setAdapter(gridAdapter);

            //for possibility download more movies
            loadMore = true;
            current_page++;
        }
    }
}
