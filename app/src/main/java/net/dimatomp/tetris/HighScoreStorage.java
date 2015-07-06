package net.dimatomp.tetris;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

import static net.dimatomp.tetris.HighScoreStorage.HighScoreColumns.TABLE_NAME;
import static net.dimatomp.tetris.HighScoreStorage.HighScoreColumns.TIME;
import static net.dimatomp.tetris.HighScoreStorage.HighScoreColumns.VALUE;
import static net.dimatomp.tetris.HighScoreStorage.HighScoreColumns._ID;

public class HighScoreStorage extends ContentProvider {
    private DBHelper instance;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        try (SQLiteDatabase database = instance.getWritableDatabase()) {
            database.insert(TABLE_NAME, null, values);
            return Uri.parse("content://net.dimatomp.tetris/high_score");
        }
    }

    @Override
    public boolean onCreate() {
        instance = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        try (SQLiteDatabase database = instance.getReadableDatabase()) {
            return database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public interface HighScoreColumns extends BaseColumns {
        String TABLE_NAME = "high_scores";
        String TIME = "Time";
        String VALUE = "Value";
    }

    private class DBHelper extends SQLiteOpenHelper {
        static final String filename = "highscore.db";

        public DBHelper(Context context) {
            super(context, filename, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TIME + " INTEGER, " +
                    VALUE + " INTEGER NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
