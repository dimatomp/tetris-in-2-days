package net.dimatomp.tetris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class TetrisView extends SurfaceView implements TetrisModel.Callback {
    public static final int FIELD_SIDE = 24;
    private static final Random rng = new Random();
    private TetrisModel model;
    private long interval;

    private Runnable moveDown = new Runnable() {
        @Override
        public void run() {
            if (interval > 0) {
                if (!model.moveY(1)) {
                    interval = 500;
                    int fType = rng.nextInt(TetrisModel.getFiguresCount());
                    int dir = rng.nextInt(TetrisModel.getPosCount(fType));
                    model.throwFigure(fType, dir);
                }
                if (interval > 0)
                    postDelayed(this, interval);
            }
        }
    };

    public TetrisView(Context context) {
        super(context);
    }

    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TetrisView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onGameOver() {
    }

    public void stopPlaying() {
        interval = 0;
    }

    @Override
    public void onLinesRemoved(int... pos) {
        int maxPos = Integer.MIN_VALUE;
        for (int p : pos)
            maxPos = Math.max(maxPos, p);
        final int fMaxPos = maxPos;
        refresh(new Rect(0, 0, model.getWidth(), fMaxPos + 1));
    }

    public TetrisModel getModel() {
        return model;
    }

    @Override
    public void onFigureMoved(Rect oldArea) {
        refresh(oldArea);
    }

    public void speedUp() {
        if (interval != 0)
            interval = 50;
    }

    public void startPlaying() {
        if (interval == 0) {
            if (model == null) {
                model = new TetrisModel(FIELD_SIDE, FIELD_SIDE);
                int fType = rng.nextInt(TetrisModel.getFiguresCount());
                int dir = rng.nextInt(TetrisModel.getPosCount(fType));
                model.placeNewFigure(fType, dir);
            }
            refresh(null);
            model.registerCallback(this);
            interval = 500;
            postDelayed(moveDown, interval);
        }
    }

    private int getHorOffset() {
        return Math.max(0, (getWidth() - getHeight()) / 2);
    }

    private int getVerOffset() {
        return Math.max(0, (getHeight() - getWidth()) / 2);
    }

    private int getXPos(int x) {
        return getHorOffset() + Math.min(getWidth(), getHeight()) * x / model.getWidth();
    }

    private int getYPos(int y) {
        return getVerOffset() + Math.min(getWidth(), getHeight()) * y / model.getHeight();
    }

    private Rect scaled(Rect dsc) {
        return new Rect(getXPos(dsc.left), getYPos(dsc.top), getXPos(dsc.right), getYPos(dsc.bottom));
    }

    private void blankArea(Canvas canvas, Rect scaled) {
        Paint rectPaint = new Paint();
        rectPaint.setColor(getResources().getColor(android.R.color.black));
        rectPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(scaled, rectPaint);
    }

    private void drawBorder(Canvas canvas) {
        Paint rectPaint = new Paint();
        rectPaint.setColor(getResources().getColor(android.R.color.white));
        rectPaint.setStyle(Paint.Style.STROKE);
        float overlap = getResources().getDimension(R.dimen.stroke_size);
        rectPaint.setStrokeWidth(overlap);
        canvas.drawRect(getXPos(0) - overlap, getYPos(0) - overlap,
                getXPos(model.getWidth()) + overlap, getYPos(model.getHeight()) + overlap, rectPaint);
    }

    private void refreshField(Canvas canvas) {
        Drawable block = getResources().getDrawable(R.drawable.block);
        float overlap = getResources().getDimension(R.dimen.stroke_size) / 2;
        for (int x = 0; x < model.getWidth(); x++)
            for (int y = 0; y < model.getHeight(); y++) {
                if (model.isOccupied(x, y)) {
                    block.setBounds((int) (getXPos(x) - overlap), (int) (getYPos(y) - overlap),
                            (int) (getXPos(x + 1) + overlap), (int) (getYPos(y + 1) + overlap));
                    block.draw(canvas);
                }
            }
    }

    private void drawFigure(Canvas canvas) {
        Drawable figure = getResources().getDrawable(R.drawable.figure);
        float overlap = getResources().getDimension(R.dimen.stroke_size) / 2;
        for (int x = Math.max(0, -model.getX()); x < model.getFigureWidth(); x++)
            for (int y = Math.max(0, -model.getY()); y < model.getFigureHeight(); y++) {
                if (model.isFigurePart(x, y)) {
                    figure.setBounds((int) (getXPos(x + model.getX()) - overlap)
                            , (int) (getYPos(y + model.getY()) - overlap)
                            , (int) (getXPos(x + model.getX() + 1) + overlap)
                            , (int) (getYPos(y + model.getY() + 1) + overlap));
                    figure.draw(canvas);
                }
            }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle result = new Bundle();
        result.putParcelable("superState", super.onSaveInstanceState());
        result.putParcelable("tetrisModel", model);
        return result;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle && ((Bundle) state).containsKey("superState")) {
            Bundle bState = (Bundle) state;
            super.onRestoreInstanceState(bState.getParcelable("superState"));
            model = bState.getParcelable("tetrisModel");
        } else
            super.onRestoreInstanceState(state);
    }

    public void refresh(Rect oldRect) {
        final SurfaceHolder holder = getHolder();
        Rect scaled;
        Rect newRect = model.getFigureRect();
        if (oldRect != null) {
            if (newRect != null) {
                oldRect.left = Math.min(oldRect.left, newRect.left);
                oldRect.top = Math.min(oldRect.top, newRect.top);
                oldRect.right = Math.max(oldRect.right, newRect.right);
                oldRect.bottom = Math.max(oldRect.bottom, newRect.bottom);
            }
            oldRect.left--;
            oldRect.top--;
            oldRect.right++;
            oldRect.bottom++;
            scaled = scaled(oldRect);
        } else {
            getDrawingRect(scaled = new Rect());
        }
        Canvas canvas = holder.lockCanvas(scaled);
        if (canvas != null) {
            try {
                blankArea(canvas, scaled);
                drawBorder(canvas);
                refreshField(canvas);
                drawFigure(canvas);
            } finally {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
