package net.dimatomp.tetris;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Loader;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static net.dimatomp.tetris.HighScoreStorage.HighScoreColumns.TIME;
import static net.dimatomp.tetris.HighScoreStorage.HighScoreColumns.VALUE;

public class MainActivity extends Activity implements TetrisModel.Callback, LoaderManager.LoaderCallbacks<Void> {
    private int points = 0;

    @Override
    public Loader<Void> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Void>(MainActivity.this) {
            @Override
            public Void loadInBackground() {
                ContentValues values = new ContentValues(2);
                values.put(TIME, args.getLong("time"));
                values.put(VALUE, args.getInt("score"));
                getContentResolver().insert(Uri.parse("content://net.dimatomp.tetris/highscore"), values);
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null && savedInstanceState.containsKey("points")) {
            points = savedInstanceState.getInt("points");
            ((TextView) findViewById(R.id.score)).setText(Integer.toString(points));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TetrisView view = (TetrisView) findViewById(R.id.field);
        view.startPlaying();
        view.getModel().registerCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((TetrisView) findViewById(R.id.field)).stopPlaying();
    }

    @Override
    public void onLinesRemoved(int... pos) {
        points += pos.length;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.score)).setText(Integer.toString(points));
            }
        });
    }

    public void speedUp(View button) {
        ((TetrisView) findViewById(R.id.field)).speedUp();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("points", points);
    }

    @Override
    public void onGameOver() {
        Bundle bundle = new Bundle(2);
        bundle.putLong("time", System.currentTimeMillis());
        bundle.putInt("value", points);
        getLoaderManager().restartLoader(0, bundle, this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onFigureMoved(Rect oldArea, Rect newArea) {
    }

    public void moveLeft(View button) {
        ((TetrisView) findViewById(R.id.field)).getModel().moveX(-1);
    }

    public void moveRight(View button) {
        ((TetrisView) findViewById(R.id.field)).getModel().moveX(1);
    }

    public void turnRight(View button) {
        ((TetrisView) findViewById(R.id.field)).getModel().turnClockwise(-1);
    }
}
