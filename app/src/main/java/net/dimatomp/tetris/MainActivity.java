package net.dimatomp.tetris;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void moveLeft(View button) {
        ((TetrisView) findViewById(R.id.field)).moveLeft();
    }

    public void moveRight(View button) {
        ((TetrisView) findViewById(R.id.field)).moveRight();
    }
}
