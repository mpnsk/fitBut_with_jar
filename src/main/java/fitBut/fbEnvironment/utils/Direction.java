package fitBut.fbEnvironment.utils;

import fitBut.utils.logging.HorseRider;

import fitBut.utils.Point;

/**
 * @author : Vaclav Uhlir
 * @since : 14.9.2019
 **/
public enum Direction {
    W("w"),
    E("e"),
    N("n"),
    S("s"),
    __UNKNOWN("");

    private String text;
    private static final String TAG = "Direction";

    Direction(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return getText();
    }

    @SuppressWarnings("unused")
    public static Direction fromString(String text) {
        for (Direction direction : Direction.values()) {
            if (direction.text.equalsIgnoreCase(text)) {
                return direction;
            }
        }
        HorseRider.yell(TAG, "fromString: unknown direction! " + text);
        return __UNKNOWN;
    }

    public static Direction getDirectionFromXY(int x, int y) {
        if (y == 0 && ((x == -1) || (Point.LoopLimit.isFoundX() && Point.LoopLimit.getLoopX() - 1 == x))) {
            return W;
        } else if (x == 1 && y == 0) {
            return E;
        } else if (x == 0 && ((y == -1) || (Point.LoopLimit.isFoundY() && Point.LoopLimit.getLoopY() - 1 == y))) {
            return N;
        } else if (x == 0 && y == 1) {
            return S;
        } else {
            HorseRider.yell(TAG, "getDirectionFromXY: unable to get direction from x:" + x + " y:" + y);
            return __UNKNOWN;
        }
    }


    public static Direction oppositeDirection(Direction dir) {
        switch (dir) {
            case W:
                return E;
            case E:
                return W;
            case N:
                return S;
            case S:
                return N;
            case __UNKNOWN:
                break;
        }

        return __UNKNOWN;
    }

    /**
     * @return vector
     */
    public Point getVector() {
        return this.directionToDelta();
    }

    /**
     * direction to vector E -> (1,0)
     *
     * @return point - vector
     */
    public Point directionToDelta() {
        int x = 0;
        int y = 0;
        switch (this) {
            case W:
                x = -1;
                break;
            case E:
                x = 1;
                break;
            case N:
                y = -1;
                break;
            case S:
                y = 1;
                break;
            case __UNKNOWN:
                break;
        }
        return (new Point(x, y));
    }

    public Direction getRotatedHeading(Rotation rotation) {
        switch (rotation) {
            case CW:
                return this.rotateClockwise();
            case CCW:
                return Direction.oppositeDirection(this).rotateClockwise();
            case NULL:
                return this;
            case OPPOSITE:
                return Direction.oppositeDirection(this);
            case __UNKNOWN:
                break;
        }
        return __UNKNOWN;
    }

    private Direction rotateClockwise() {
        switch (this) {
            case W:
                return N;
            case E:
                return S;
            case N:
                return E;
            case S:
                return W;
        }
        return __UNKNOWN;
    }

    /**
     * gets rotation needed to target
     * (self N ,target E -> CW)
     *
     * @param target target rotation
     * @return rotation
     */
    public Rotation rotationTo(Direction target) {
        if (this == target) {
            return Rotation.NULL;
        } else if (Direction.oppositeDirection(this) == target) {
            return Rotation.OPPOSITE;
        } else if (this.getRotatedHeading(Rotation.CW) == target) {
            return Rotation.CW;
        }
        if (this.getRotatedHeading(Rotation.CCW) == target) {
            return Rotation.CCW;
        }
        return Rotation.__UNKNOWN;
    }
}
