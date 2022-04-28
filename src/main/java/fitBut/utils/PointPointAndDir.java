package fitBut.utils;

import fitBut.fbEnvironment.utils.Direction;

/**
 * @author : Vaclav Uhlir
 * @since : 08/03/2021
 **/

public class PointPointAndDir {
    private final Point position1;
    private final Point position2;
    private final Direction heading;

    @Override
    public String toString() {
        return position1.toString() + " " + position2.toString() + " " + heading;
    }

    public PointPointAndDir(Point position1, Point position2, Direction heading) {
        this.position1 = position1;
        this.position2 = position2;
        this.heading = heading;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PointPointAndDir) {
            PointPointAndDir pptdr = (PointPointAndDir) obj;
            return (position1.equals(pptdr.position1)) && (position2.equals(pptdr.position2)) && (heading == pptdr.heading);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return position1.hashCode() + position2.hashCode() + heading.hashCode();
    }

    public Point getPoint1() {
        return position1;
    }

    public Point getPoint2() {
        return position2;
    }

    public Direction getDir() {
        return heading;
    }
}
