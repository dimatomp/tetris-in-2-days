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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TetrisView extends SurfaceView implements SurfaceHolder.Callback, TetrisModel.Callback {
    private static final Random rng = new Random();
    private final Paint rectPaint = new Paint();
    private TetrisModel model;
    private ScheduledFuture updater;
    private ScheduledExecutorService renderThread = Executors.newSingleThreadScheduledExecutor();

    public TetrisView(Context context) {
        super(context);
        rectPaint.setStyle(Paint.Style.FILL);
        getHolder().addCallback(this);
    }

    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rectPaint.setStyle(Paint.Style.FILL);
        getHolder().addCallback(this);
    }

    public TetrisView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        rectPaint.setStyle(Paint.Style.FILL);
        getHolder().addCallback(this);
    }

    public void stopPlaying() {
        if (updater == null)
            return;
        updater.cancel(true);
        updater = null;
    }

    @Override
    public void onLinesRemoved(int... pos) {
        int maxPos = Integer.MIN_VALUE;
        for (int p : pos)
            maxPos = Math.max(maxPos, p);
        final int fMaxPos = maxPos;
        renderThread.execute(new Runnable() {
            @Override
            public void run() {
                refresh(new Rect(0, 0, model.getWidth(), fMaxPos + 1), null);
            }
        });
    }

    public TetrisModel getModel() {
        return model;
    }

    @Override
    public void onFigureMoved(final Rect oldArea, final Rect newArea) {
        renderThread.execute(new Runnable() {
            @Override
            public void run() {
                refresh(oldArea, newArea);
            }
        });
    }

    public void startPlaying() {
        if (updater != null)
            return;
        if (model == null)
            model = new TetrisModel(15, 10);
        else
            renderThread.execute(new Runnable() {
                @Override
                public void run() {
                    Rect field = new Rect(0, 0, model.getWidth(), model.getHeight());
                    refresh(field, field);
                }
            });
        model.registerCallback(this);
        updater = renderThread.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (!model.moveY(1)) {
                    int fType = rng.nextInt(TetrisModel.getFiguresCount());
                    int dir = rng.nextInt(TetrisModel.getPosCount(fType));
                    model.throwFigure(fType, dir);
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPlaying();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPlaying();
    }

    private Rect scaled(Rect dsc) {
        return new Rect(getWidth() * dsc.left / model.getWidth(),
                getHeight() * dsc.top / model.getHeight(),
                getWidth() * dsc.right / model.getWidth(),
                getHeight() * dsc.bottom / model.getHeight());
    }

    private void refresh(Rect oldRect, Rect newRect) {
        final SurfaceHolder holder = getHolder();
        synchronized (holder) {
            synchronized (model) {
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
                Rect scaled = scaled(oldRect);
                Canvas canvas = holder.lockCanvas(scaled);
                if (canvas != null) {
                    try {
                        blankArea(canvas, scaled);
                        refreshField(canvas);
                        drawFigure(canvas);
                    } finally {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    private void blankArea(Canvas canvas, Rect scaled) {
        rectPaint.setColor(getResources().getColor(android.R.color.black));
        canvas.drawRect(scaled, rectPaint);
    }

    private void refreshField(Canvas canvas) {
        Drawable block = getResources().getDrawable(R.drawable.block);
        float overlap = getResources().getDimension(R.dimen.overlap);
        rectPaint.setColor(getResources().getColor(R.color.block));
        for (int x = 0; x < model.getWidth(); x++)
            for (int y = 0; y < model.getHeight(); y++) {
                if (model.isOccupied(x, y)) {
                    block.setBounds((int) (getWidth() * x / model.getWidth() - overlap)
                            , (int) (getHeight() * y / model.getHeight() - overlap)
                            , (int) (getWidth() * (x + 1) / model.getWidth() + overlap)
                            , (int) (getHeight() * (y + 1) / model.getHeight() + overlap));
                    block.draw(canvas);
                }
            }
    }

    private void drawFigure(Canvas canvas) {
        Drawable figure = getResources().getDrawable(R.drawable.figure);
        float overlap = getResources().getDimension(R.dimen.overlap);
        for (int x = 0; x < model.getFigureWidth(); x++)
            for (int y = 0; y < model.getFigureHeight(); y++) {
                if (model.isFigurePart(x, y)) {
                    figure.setBounds((int) (getWidth() * (x + model.getX()) / model.getWidth() - overlap)
                            , (int) (getHeight() * (y + model.getY()) / model.getHeight() - overlap)
                            , (int) (getWidth() * (x + model.getX() + 1) / model.getWidth() + overlap)
                            , (int) (getHeight() * (y + model.getY() + 1) / model.getHeight() + overlap));
                    figure.draw(canvas);
                }
            }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle result = new Bundle();
        result.putParcelable("superState", super.onSaveInstanceState());
        synchronized (model) {
            result.putParcelable("tetrisModel", model);
        }
        return result;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle && ((Bundle) state).containsKey("superState")) {
            super.onRestoreInstanceState(((Bundle) state).getParcelable("superState"));
            model = ((Bundle) state).getParcelable("tetrisModel");
        } else
            super.onRestoreInstanceState(state);
    }
}
