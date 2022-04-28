package fitBut.utils;

import fitBut.fbEnvironment.utils.Direction;

/**
 * @author : Vaclav Uhlir
 * @since : 28/09/2019
 **/

public class PointAndDir {
    private final Point position;
    private final Direction heading;

    @Override
    public String toString() {
        return position.toString() + " " + heading;
    }

    public PointAndDir(Point position, Direction heading) {
        this.position = position;
        this.heading = heading;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PointAndDir) {
            PointAndDir ptdr = (PointAndDir) obj;
            return (position.equals(ptdr.position)) && (heading == ptdr.heading);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return position.hashCode() + heading.hashCode();
    }

    public Point getPoint() {
        return position;
    }

    public Direction getDir() {
        return heading;
    }
}
