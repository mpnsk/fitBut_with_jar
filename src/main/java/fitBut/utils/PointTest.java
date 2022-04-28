package fitBut.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointTest {

    @Test
    void distance() {
    }

    @Test
    void limitedDistance() {
        Point agent = new Point(34, 30);
        Point taskboard = new Point(32, 65);
        int result = -1;

        result = taskboard.limitedDistance(agent);
        assertEquals(37, result);

        Point.LoopLimit.setLoopX(70);
        result = taskboard.limitedDistance(agent);
        assertEquals(37, result);


        Point.LoopLimit.setLoopY(70);
        result = taskboard.limitedDistance(agent);
        assertEquals(37, result);

        agent = new Point(32, 30);
        result = taskboard.limitedDistance(agent);
        assertEquals(35, result);

        taskboard = new Point(32, 67);
        result = taskboard.limitedDistance(agent);
        assertEquals(33, result);

        taskboard = new Point(32, 63);
        result = taskboard.limitedDistance(agent);
        assertEquals(33, result);
    }
}