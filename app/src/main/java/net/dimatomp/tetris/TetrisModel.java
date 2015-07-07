package net.dimatomp.tetris;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TetrisModel implements Parcelable {
    // Currently supplying all turned states explicitly.
    // Array of figures, each one represented as a sequence of states.
    private static final boolean[][][][] FIGURES = new boolean[][][][] {
            new boolean[][][]{
                    reformat(".#", ".#", ".#", ".#"),
                    reformat("....", "####")
            },
            new boolean[][][]{
                    reformat(".#.", ".##", ".#."),
                    reformat("...", "###", ".#."),
                    reformat(".#", "##", ".#"),
                    reformat(".#.", "###")
            },
            new boolean[][][]{
                    reformat("#.", "##", ".#"),
                    reformat(".##", "##.")
            },
            new boolean[][][]{
                    reformat(".#", "##", "#."),
                    reformat("##.", ".##")
            },
            new boolean[][][] {
                    reformat(".#.", ".#.", ".##"),
                    reformat("...", "###", "#.."),
                    reformat("##", ".#", ".#"),
                    reformat("..#", "###")
            },
            new boolean[][][] {
                    reformat(".#", ".#", "##"),
                    reformat("#..", "###"),
                    reformat(".##", ".#.", ".#."),
                    reformat("...", "###", "..#")
            },
            new boolean[][][]{
                    reformat("##", "##")
            }
    };
    private static final Random rng = new Random();
    private final Map<Callback, Object> callbacks = new IdentityHashMap<>();
    private int figureType;
    private int turnDegree;
    private int figurePosX;
    private int figurePosY;
    private boolean field[][];
    public static final Creator<TetrisModel> CREATOR = new Creator<TetrisModel>() {
        @Override
        public TetrisModel createFromParcel(Parcel source) {
            TetrisModel model = new TetrisModel();
            model.figureType = source.readInt();
            model.turnDegree = source.readInt();
            model.figurePosX = source.readInt();
            model.figurePosY = source.readInt();
            model.field = (boolean[][]) source.readSerializable();
            return model;
        }

        @Override
        public TetrisModel[] newArray(int size) {
            return new TetrisModel[size];
        }
    };
    private TetrisModel(){}
    public TetrisModel(int fieldWidth, int fieldHeight) {
        this.field = new boolean[fieldWidth][fieldHeight];
    }

    private static boolean[][] reformat(String... rows) {
        boolean[][] result = new boolean[rows.length][rows[0].length()];
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < rows[i].length(); j++)
                result[i][j] = (rows[i].charAt(j) == '#');
        }
        return result;
    }

    public static int getFiguresCount() {
        return FIGURES.length;
    }

    public static int getPosCount(int figType) {
        return FIGURES[figType].length;
    }

    public void registerCallback(Callback callback) {
        callbacks.put(callback, this);
    }

    private void notify(Consumer func) {
        for (Callback callback : callbacks.keySet())
            func.apply(callback);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(figureType);
        dest.writeInt(turnDegree);
        dest.writeInt(figurePosX);
        dest.writeInt(figurePosY);
        dest.writeSerializable(field);
    }

    public int getFigureType() {
        return figureType;
    }

    public void setFigureType(int figureType) {
        this.figureType = figureType;
    }

    public int getTurnDegree() {
        return turnDegree;
    }

    public void setTurnDegree(int turnDegree) {
        this.turnDegree = (turnDegree + getPosCount(figureType)) % getPosCount(figureType);
    }

    public int getWidth() {
        return field.length;
    }

    public int getHeight() {
        return field[0].length;
    }

    public boolean isOccupied(int x, int y) {
        return field[x][y];
    }

    public int getX() {
        return figurePosX;
    }

    public void setX(int x) {
        figurePosX = x;
    }

    public int getY() {
        return figurePosY;
    }

    public void setY(int y) {
        figurePosY = y;
    }

    public int getFigureWidth() {
        return FIGURES[figureType][turnDegree].length;
    }

    public int getFigureHeight() {
        return FIGURES[figureType][turnDegree][0].length;
    }

    public boolean isFigurePart(int x, int y) {
        return FIGURES[figureType][turnDegree][x][y];
    }

    public boolean moveX(final int dx) {
        final Rect oldRect;
        oldRect = getFigureRect();
        setX(getX() + dx);
        if (!isValidState()) {
            setX(getX() - dx);
            return false;
        }
        notify(new Consumer() {
            @Override
            public void apply(Callback callback) {
                callback.onFigureMoved(oldRect);
            }
        });
        return true;
    }

    public boolean moveY(final int dy) {
        final Rect oldRect = getFigureRect();
        setY(getY() + dy);
        if (!isValidState()) {
            setY(getY() - dy);
            return false;
        }
        notify(new Consumer() {
            @Override
            public void apply(Callback callback) {
                callback.onFigureMoved(oldRect);
            }
        });
        return true;
    }

    private boolean isValidState() {
        for (int x = 0; x < getFigureWidth(); x++)
            for (int y = 0; y < getFigureHeight(); y++)
                if (isFigurePart(x, y) && (x + getX() < 0 || x + getX() >= getWidth()
                        || y + getY() >= getHeight()
                        || y + getY() >= 0 && isOccupied(x + getX(), y + getY())))
                    return false;
        return true;
    }

    public boolean turnClockwise(int rot) {
        final Rect oldRect;
        oldRect = getFigureRect();
        setTurnDegree(getTurnDegree() + rot);
        if (!isValidState()) {
            setTurnDegree(getTurnDegree() - rot);
            return false;
        }
        notify(new Consumer() {
            @Override
            public void apply(Callback callback) {
                callback.onFigureMoved(oldRect);
            }
        });
        return true;
    }

    public void throwFigure(int figType, int degree) {
        final Rect oldArea;
        List<Integer> remLines = new ArrayList<>();
        oldArea = getFigureRect();
        for (int y = Math.max(-getY(), 0) - 1; y >= 0; y--) {
            for (int x = 0; x < getFigureWidth(); x++)
                if (isFigurePart(x, y)) {
                    field = new boolean[field.length][field[0].length];
                    notify(new Consumer() {
                        @Override
                        public void apply(Callback callback) {
                            callback.onGameOver();
                    }
                    });
                    return;
                }
        }
        for (int x = Math.max(0, -getX()); x < getFigureWidth() && x + getX() < getWidth(); x++)
            for (int y = Math.max(0, -getY()); y < getFigureHeight() && y + getY() < getHeight(); y++) {
                field[x + getX()][y + getY()] |= isFigurePart(x, y);
            }
        for (int y = Math.max(0, -getY()); y < getFigureHeight() && y + getY() < getHeight(); y++) {
            boolean ok = true;
            for (int x = 0; ok && x < getWidth(); x++)
                ok = isOccupied(x, y + getY());
            if (ok) {
                for (int x = 0; x < getWidth(); x++) {
                    System.arraycopy(field[x], 0, field[x], 1, y + getY());
                    field[x][0] = false;
                }
                remLines.add(y + getY());
            }
        }
        placeNewFigure(figType, degree);
        if (!isValidState()) {
            notify(new Consumer() {
                @Override
                public void apply(Callback callback) {
                    callback.onGameOver();
                }
            });
            return;
        }
        final int[] result = new int[remLines.size()];
        for (int i = 0; i < remLines.size(); i++)
            result[i] = remLines.get(i);
        notify(new Consumer() {
            @Override
            public void apply(Callback callback) {
                if (result.length != 0) {
                    callback.onLinesRemoved(result);
                }
                callback.onFigureMoved(oldArea);
            }
        });
    }

    public void placeNewFigure(int figType, int degree) {
        setFigureType(figType);
        setTurnDegree(degree);
        int xMin = 0;
        minLoop:
        while (true) {
            for (int y = 0; y < getFigureHeight(); y++)
                if (isFigurePart(xMin, y))
                    break minLoop;
            xMin++;
        }
        int xMax = getFigureWidth();
        maxLoop:
        while (true) {
            for (int y = 0; y < getFigureHeight(); y++)
                if (isFigurePart(xMax - 1, y))
                    break maxLoop;
            xMax--;
        }
        int interval = getWidth() + xMin - xMax;
        figurePosX = rng.nextInt(interval) - xMin;
        figurePosY = -getFigureHeight() + 1;
    }

    public Rect getFigureRect() {
        return new Rect(getX(), getY(), getX() + getFigureWidth(), getY() + getFigureHeight());
    }

    public interface Callback {
        void onLinesRemoved(int... pos);

        void onFigureMoved(Rect oldArea);

        void onGameOver();
    }

    private interface Consumer {
        void apply(Callback callback);
    }
}
