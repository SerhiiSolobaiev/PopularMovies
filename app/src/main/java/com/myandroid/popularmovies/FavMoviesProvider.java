package com.myandroid.popularmovies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class FavMoviesProvider extends ContentProvider{

    private final String LOG_TAG = FavMoviesProvider .class.getSimpleName();

    private static final String DATABASE_NAME = "movies";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "movies";

    public static final String ID_MOVIE = "_id";
    public static final String TITLE = "title";
    public static final String IMAGE = "image";
    public static final String OVERVIEW = "overview";
    public static final String VOTE_AVERAGE = "vote_average";
    public static final String RELEASE_DATE = "release_date";
    public static final String IS_FAVORITE = "is_favorite";

    final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + ID_MOVIE + " INTEGER UNIQUE,"
            + TITLE + " TEXT,"
            + IMAGE + " BLOB,"
            + OVERVIEW + " TEXT,"
            + VOTE_AVERAGE + " REAL,"
            + RELEASE_DATE + " TEXT,"
            + IS_FAVORITE + " INTEGER" + ")";

    private static final String AUTHORITY = "com.myandroid.popularmovies.Movies";
    static final String MOVIES_PATH = "movies";

    public static final Uri MOVIE_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + MOVIES_PATH);

    static final String MOVIE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + MOVIES_PATH;

    static final String MOVIE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + MOVIES_PATH;

    static final int URI_MOVIES = 1;
    static final int URI_MOVIES_ID = 2;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MOVIES_PATH, URI_MOVIES);
        uriMatcher.addURI(AUTHORITY, MOVIES_PATH + "/#", URI_MOVIES_ID);
    }

    DBHelper dbHelper;
    SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Log.d(LOG_TAG, "query, uri = " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_MOVIES:
                Log.d(LOG_TAG, "URI_MOVIES");
                break;

            case URI_MOVIES_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_MOVIES_ID = " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ID_MOVIE + " = " + id;
                } else {
                    selection = selection + " AND " + ID_MOVIE + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI = " + uri);
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null,
                sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), MOVIE_CONTENT_URI);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(LOG_TAG, "getType, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_MOVIES:
                return MOVIE_CONTENT_TYPE;
            case URI_MOVIES_ID:
                return MOVIE_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert, uri = " + uri.toString());
        if (uriMatcher.match(uri) != URI_MOVIES)
            throw new IllegalArgumentException("Wrong URI = " + uri);

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(TABLE_NAME, null, values);
        Uri resultUri = ContentUris.withAppendedId(MOVIE_CONTENT_URI, rowID);

        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete, uri = " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_MOVIES:
                Log.d(LOG_TAG, "URI_MOVIES");
                break;

            case URI_MOVIES_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_MOVIES_ID = " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ID_MOVIE + " = " + id;
                } else {
                    selection = selection + " AND " + ID_MOVIE + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI = " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "update, uri = " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_MOVIES:
                Log.d(LOG_TAG, "URI_MOVIES");
                break;

            case URI_MOVIES_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_MOVIES_ID = " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ID_MOVIE + " = " + id;
                } else {
                    selection = selection + " AND " + ID_MOVIE + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI = " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.update(TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }


    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(QUERY_CREATE_TABLE);
//            ContentValues cv = new ContentValues();
//            for (int i = 1; i <= 3; i++) {
//                cv.put(CONTACT_NAME, "name " + i);
//                cv.put(CONTACT_EMAIL, "email " + i);
//                db.insert(CONTACT_TABLE, null, cv);
//            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
