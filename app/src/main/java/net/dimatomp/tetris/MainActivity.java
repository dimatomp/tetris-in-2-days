package net.dimatomp.tetris;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements TetrisModel.Callback {
    private int points = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null && savedInstanceState.containsKey("points"))
            points = savedInstanceState.getInt("points");
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
