package fitBut.fbMultiagent;

import fitBut.fbPerceptionModule.data.BlockType;
import fitBut.utils.Point;

import java.util.HashMap;

public class FBTask {
    private String name;
    private int deadline;
    private int reward;
    private HashMap<Point, BlockType> taskBody;
    private HashMap<BlockType, Integer> typesNeeded = new HashMap<>();

    public HashMap<Point, BlockType> getTaskBody() {
        return taskBody;
    }

    public String getName() {
        return (name);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FBTask) {
            return ((FBTask) obj).getName().equals(getName());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public HashMap<BlockType, Integer> getTypesNeeded() {
        return (typesNeeded);
    }

    public FBTask(String taskName, int taskDeadline, int taskReward, HashMap<Point, BlockType> structureMap) {
        name = taskName;
        deadline = taskDeadline;
        reward = taskReward;
        taskBody = structureMap;
        getBlocksNeededList();

    }

    private void getBlocksNeededList() {
        taskBody.forEach((point, blockType) ->
                typesNeeded.put(
                        blockType,
                        typesNeeded.getOrDefault(blockType, 0) + 1));
    }

    public int getDeadline() {
        return deadline;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        string.append(name).append("\n");
        Point min = new Point(0, 0);
        Point max = new Point(0, 0);
        for (Point point : taskBody.keySet()) {
            min = Point.min(min, point);
            max = Point.max(max, point);
        }
        for (int y = min.y; y <= max.y; y++) {
            for (int x = min.x; x <= max.x; x++) {
                BlockType block = taskBody.get(new Point(x, y));
                if (block != null) {
                    string.append(block.getName()).append(" ");
                } else if (x == 0 && y == 0) {
                    string.append("X  ");
                } else {
                    string.append("   ");
                }
            }
            string.append("\n");
        }
        return string.toString();
    }

    public int getReward() {
        return reward;
    }

    public double getBodySize() {
        return taskBody.size();
    }
}
