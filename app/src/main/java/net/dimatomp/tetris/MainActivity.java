package net.dimatomp.tetris;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static net.dimatomp.tetris.HighScoreStorage.HighScoreColumns.TIME;
import static net.dimatomp.tetris.HighScoreStorage.HighScoreColumns.VALUE;

public class MainActivity extends Activity implements TetrisModel.Callback {
    private int points = 0;
    private TetrisView tetrisView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tetrisView = (TetrisView) findViewById(R.id.field);
        if (savedInstanceState != null && savedInstanceState.containsKey("points")) {
            points = savedInstanceState.getInt("points");
            ((TextView) findViewById(R.id.score)).setText(Integer.toString(points));
        }
    }

    @Override
    protected void onResume() {
        super.onStart();
        tetrisView.startPlaying();
        tetrisView.getModel().registerCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tetrisView.stopPlaying();
    }

    @Override
    protected void onDestroy() {
        tetrisView.shutdownGameThread();
        super.onDestroy();
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
        tetrisView.runOnGameThread(new Runnable() {
            @Override
            public void run() {
                tetrisView.speedUp();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("points", points);
    }

    @Override
    public void onGameOver() {
        tetrisView.stopPlaying();
        if (points > 0) {
            ContentValues values = new ContentValues(2);
            values.put(TIME, System.currentTimeMillis());
            values.put(VALUE, points);
            getContentResolver().insert(Uri.parse("content://net.dimatomp.tetris/highscore"), values);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onFigureMoved(Rect oldArea) {
    }

    public void moveLeft(View button) {
        tetrisView.runOnGameThread(new Runnable() {
            @Override
            public void run() {
                tetrisView.getModel().moveX(-1);
            }
        });
    }

    public void moveRight(View button) {
        tetrisView.runOnGameThread(new Runnable() {
            @Override
            public void run() {
                tetrisView.getModel().moveX(1);
            }
        });
    }

    public void turnRight(View button) {
        tetrisView.runOnGameThread(new Runnable() {
            @Override
            public void run() {
                tetrisView.getModel().turnClockwise(-1);
            }
        });
    }
}
