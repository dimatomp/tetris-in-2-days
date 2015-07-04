package net.dimatomp.tetris;

import android.os.Parcel;
import android.os.Parcelable;

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
                    reformat("#..", "##.", ".#."),
                    reformat(".##", "##.", "...")
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

    private static boolean[][] reformat(String... rows) {
        boolean[][] result = new boolean[rows.length][rows[0].length()];
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < rows[i].length(); j++)
                result[i][j] = (rows[i].charAt(j) == '#');
        }
        return result;
    }

    private int figureType;
    private int turnDegree;
    private int figurePosX;
    private int figurePosY;
    private boolean field[][];

    private TetrisModel(){}

    public TetrisModel(int fieldWidth, int fieldHeight) {
        this.field = new boolean[fieldWidth][fieldHeight];
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

    public static int getFiguresCount() {
        return FIGURES.length;
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
        this.turnDegree = (turnDegree + FIGURES[figureType].length) % FIGURES[figureType].length;
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

    public int getY() {
        return figurePosY;
    }

    public void setX(int x) {
        figurePosX = x;
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

    public synchronized boolean moveX(int dx) {
        setX(getX() + dx);
        if (!isValidState()) {
            setX(getX() - dx);
            return false;
        }
        return true;
    }

    public synchronized boolean moveY(int dy) {
        setY(getY() + dy);
        if (!isValidState()) {
            setY(getY() - dy);
            return false;
        }
        return true;
    }

    public synchronized boolean isValidState() {
        for (int x = 0; x < getFigureWidth(); x++)
            for (int y = 0; y < getFigureHeight(); y++)
                if (isFigurePart(x, y) && (x + getX() < 0 || x + getX() >= getWidth()
                        || y + getY() >= getHeight()
                        || y + getY() >= 0 && isOccupied(x + getX(), y + getY())))
                    return false;
        return true;
    }

    public synchronized boolean turnClockwise(int rot) {
        setTurnDegree(getTurnDegree() + rot);
        if (!isValidState()) {
            setTurnDegree(getTurnDegree() - rot);
            return false;
        }
        return true;
    }
}
