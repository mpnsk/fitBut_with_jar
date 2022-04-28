package fitBut.utils;

import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.utils.logging.HorseRider;

import java.util.HashSet;
import java.util.Set;

import static fitBut.fbEnvironment.utils.Direction.*;

/**
 * @author : Vaclav Uhlir
 * @since : 16/09/2019
 **/

public class Point extends java.awt.Point {
    private static final int NUM_ALIGN = 2;
    private static final String TAG = "Point";

    public Point(int x, int y) {
        super(x, y);
    }

    public Point(Point vector) {
        super(vector.x, vector.y);
    }

    public static Point zero() {
        return new Point(0, 0);
    }

    public static Point min(Point point1, Point point2) {
        return new Point(Math.min(point1.x, point2.x), Math.min(point1.y, point2.y));
    }

    public static Point max(Point point1, Point point2) {
        return new Point(Math.max(point1.x, point2.x), Math.max(point1.y, point2.y));
    }

    public static boolean isPositive(Point vector) {
        return vector.x >= 0 && vector.y >= 0;
    }

    public static Point unitY() {
        return new Point(0, 1);
    }

    public Point add(Point point) {
        this.x += point.x;
        this.y += point.y;
        return this;
    }

    public Point sub(Point point) {
        this.x -= point.x;
        this.y -= point.y;
        return this;
    }

    /**
     * Manhattan distance
     *
     * @param b - point to the comparison distance
     * @return abs(a.x - b.x)+abs(a.y-b.y)
     */
    public int distance(Point b) {
        return (Math.abs(this.x - b.x) + Math.abs(this.y - b.y)); // Manhattan
    }

    // distance but with looping limits in mind
    public int limitedDistance(Point target) {
        int x;
        if (LoopLimit.isFoundX()) {
            x = mod(Math.abs(this.x - target.x),(LoopLimit.getLoopX()));
            if(x>LoopLimit.getLoopX()/2) x = LoopLimit.getLoopX()-x;
        } else {
            x = Math.abs(this.x - target.x);
        }
        int y;
        if (LoopLimit.isFoundY()) {
            y = mod(Math.abs(this.y - target.y),(LoopLimit.getLoopY()));
            if(y>LoopLimit.getLoopY()/2) y = LoopLimit.getLoopY()-y;
        } else {
            y = Math.abs(this.y - target.y);
        }
        return (x+y);
    }

    private int mod(int base, int modValue){
        base = base % modValue;
        if (base < 0) base += modValue;
        return base;
    }


    @Override
    public String toString() {
        String xs = "" + x;
        String ys = "" + y;
        return "[" + " ".repeat(Math.max(0, NUM_ALIGN - xs.length())) + xs +
                "," + " ".repeat(Math.max(0, NUM_ALIGN - ys.length())) + ys + "]";
    }

    public void translate(Point vector) {
        this.add(vector);
    }

    /**
     * new difference vector
     *
     * @param diffTo target
     * @return vector
     */
    public Point diff(Point diffTo) {
        return new Point(this.x - diffTo.x, this.y - diffTo.y);
    }

    /**
     * new sum vector
     *
     * @param point target
     * @return vector
     */
    public Point sum(Point point) {
        return new Point(this.x + point.x, this.y + point.y);
    }

    /**
     * gets Majority direction
     *
     * @return Direction
     */
    public Direction getDirection() {
        if (x < 0 && Math.abs(x) > Math.abs(y)) {
            return W;
        } else if (x > 0 && Math.abs(x) > Math.abs(y)) {
            return E;
        } else if (y < 0) {
            return N;
        } else if (y > 0) {
            return S;
        } else {
            HorseRider.yell(TAG, "getRotation: wrong direction x:" + x + " y:" + y);
            return __UNKNOWN;
        }

    }

    public Point getRotated(Rotation rotation) {
        return new Point(this).rotate(rotation);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Point rotate(Rotation rotation) {
        int temp;
        switch (rotation) {
            case CW:
                temp = x;
                this.x = -y;
                this.y = temp;
                break;
            case CCW:
                temp = x;
                this.x = y;
                this.y = -temp;
                break;
            case OPPOSITE:
                this.x = -x;
                this.y = -y;
                break;
            case NULL:
            case __UNKNOWN:
                break;
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point pt = (Point) obj;
            boolean same;
            if (LoopLimit.isFoundX()) {
                same = (x % LoopLimit.getLoopX() == pt.x % LoopLimit.getLoopX());
            } else {
                same = (x == pt.x);
            }
            if (LoopLimit.isFoundY()) {
                same = (same && (y % LoopLimit.getLoopY() == pt.y % LoopLimit.getLoopY()));
            } else {
                same = (same && (y == pt.y));
            }
            return same;
        }
        return super.equals(obj);
    }

    public int size() {
        return Math.abs(x) + Math.abs(y);
    }

    public void limit() {
        LoopLimit.limit(this);
    }

    public Point getLimited() {
        return LoopLimit.getLimited(this);
    }


    public static class LoopLimit {
        private static boolean foundX = false;
        private static boolean foundY = false;
        private static int loopX;
        private static int loopY;
        private static short limitVersion = 0;

        public static void reset() {
            foundX = false;
            foundY = false;
            limitVersion = 0;
        }

        static void limit(Point point) {
            if (foundX) {
                point.x = point.x % loopX;
                if (point.x < 0) point.x += loopX;
            }
            if (foundY) {
                point.y = point.y % loopY;
                if (point.y < 0) point.y += loopY;
            }
        }

        static Point getLimited(Point point) {
            Point limited = new Point(point);
            limited.limit();
            return limited;
        }

        public static int getLoopX() {
            return loopX;
        }

        public static int getLoopY() {
            return loopY;
        }

        public static boolean setLoopX(int newLoopX) {
            if (foundX && loopX != newLoopX) {
                loopX = findGCD(loopX, newLoopX);
                limitVersion++;
                return true;
            } else if (!foundX) {
                loopX = newLoopX;
                limitVersion++;
                foundX = true;
                return true;
            }
            return false;
        }

        public static boolean setLoopY(int newLoopY) {
            if (foundY && loopY != newLoopY) { // found and different
                loopY = findGCD(loopY, newLoopY);
                limitVersion++;
                return true;
            } else if (!foundY) {
                loopY = newLoopY;
                limitVersion++;
                foundY = true;
                return true;
            }
            return false;
        }

        private static int findGCD(int loop1, int loop2) {
            return Math.min(loop1, Math.min(loop2, Math.abs(loop2 - loop1))); //not GCD but should be more than sufficient
        }

        public static boolean isFoundX() {
            return foundX;
        }

        public static boolean isFoundY() {
            return foundY;
        }

        public static short getLoopLimitVersion() {
            return limitVersion;
        }

        public static Set<Point> limitList(Set<Point> pBodyList) {
            if (pBodyList == null) return null;
            HashSet<Point> returnList = new HashSet<>();
            for (Point point : pBodyList) {
                returnList.add(point.getLimited());
            }
            return returnList;
        }
    }
}
