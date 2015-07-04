package net.dimatomp.tetris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by dimatomp on 03.07.15.
 */
public class TetrisView extends SurfaceView implements SurfaceHolder.Callback {
    private TetrisModel model;
    private ScheduledFuture updater, leftRight;
    private ScheduledExecutorService renderThread = Executors.newSingleThreadScheduledExecutor();
    private final Paint rectPaint = new Paint();

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
        updater.cancel(true);
    }

    public void moveLeft() {
        final Rect old = getFigureRect();
        if (model.moveX(-1)) {
            renderThread.execute(new Runnable() {
                @Override
                public void run() {
                    refresh(old);
                }
            });
        }
    }

    public void moveRight() {
        final Rect old = getFigureRect();
        if (model.moveX(1)) {
            renderThread.execute(new Runnable() {
                @Override
                public void run() {
                    refresh(old);
                }
            });
        }
    }

    public TetrisModel getModel() {
        return model;
    }

    public void startPlaying() {
        model = new TetrisModel(40, 60);
        updater = renderThread.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Rect old = getFigureRect();
                model.moveY(1);
                refresh(old);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public Rect getFigureRect() {
        return new Rect(model.getX(), model.getY(),
                model.getX() + model.getFigureWidth(), model.getY() + model.getFigureHeight());
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

    private void refresh(Rect oldRect) {
        SurfaceHolder holder = getHolder();
        Canvas canvas = null;
        try {
            synchronized (model) {
                Rect newRect = getFigureRect();
                if (oldRect != null)
                    newRect = new Rect(Math.min(oldRect.left, newRect.left),
                           Math.min(oldRect.top, newRect.top),
                           Math.max(oldRect.right, newRect.right),
                           Math.max(oldRect.bottom, newRect.bottom));
                Rect scaled = new Rect(newRect.left * getWidth() / model.getWidth(),
                        newRect.top * getHeight() / model.getHeight(),
                        newRect.right * getWidth() / model.getWidth(),
                        newRect.bottom * getHeight() / model.getHeight());
                canvas = holder.lockCanvas(scaled);
                rectPaint.setColor(getResources().getColor(android.R.color.black));
                canvas.drawRect(scaled, rectPaint);
                rectPaint.setColor(getResources().getColor(R.color.block));
                for (int x = newRect.left; x < newRect.right; x++)
                    for (int y = newRect.top; y < newRect.bottom; y++) {
                        if (model.isOccupied(x, y)) {
                            canvas.drawRect(getWidth() * x / model.getWidth()
                                    , getHeight() * y / model.getHeight()
                                    , getWidth() * (x + 1) / model.getWidth()
                                    , getHeight() * (y + 1) / model.getHeight(), rectPaint);
                        }
                    }
                rectPaint.setColor(getResources().getColor(R.color.figure));
                for (int x = 0; x < model.getFigureWidth(); x++)
                    for (int y = 0; y < model.getFigureHeight(); y++) {
                        if (model.isFigurePart(x, y)) {
                            canvas.drawRect(getWidth() * (x + model.getX()) / model.getWidth()
                                    , getHeight() * (y + model.getY()) / model.getHeight()
                                    , getWidth() * (x + model.getX() + 1) / model.getWidth()
                                    , getHeight() * (y + model.getY() + 1) / model.getHeight(), rectPaint);
                        }
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
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
