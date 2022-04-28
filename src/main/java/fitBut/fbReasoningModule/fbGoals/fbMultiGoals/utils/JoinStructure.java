package fitBut.fbReasoningModule.fbGoals.fbMultiGoals.utils;

import fitBut.fbEnvironment.utils.Direction;
import fitBut.utils.Point;

public class JoinStructure {
    //(master point of connection, direction to slave), (slave last point, direction from where to disconnect)
    Point mastersConnection;
    Direction fromMasterToSlave;
    Point slavesLastPoint;
    Direction slavesInnerSeparation;

    public JoinStructure(Point mastersConnection, Direction fromMasterToSlave, Point slavesLastPoint, Direction slavesInnerSeparation) {
        this.mastersConnection = mastersConnection;
        this.fromMasterToSlave = fromMasterToSlave;
        this.slavesLastPoint = slavesLastPoint;
        this.slavesInnerSeparation = slavesInnerSeparation;
    }

    public Point getMasterPoint() {
        return mastersConnection;
    }

    public Direction getDirToSlave() {
        return fromMasterToSlave;
    }

    public Point getSlaveLastPoint() {
        return slavesLastPoint;
    }

    public Direction getSlaveSeparationDir() {
        return slavesInnerSeparation;
    }
}
