package fitBut.fbReasoningModule.fbGoals;


import fitBut.agents.FBAgent;
import fitBut.fbActions.FBAttach;
import fitBut.fbActions.FBRequest;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.FBCells.objects.FBDispenserObject;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbPerceptionModule.data.BlockType;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.fbReasoningModule.fbGoals.utils.PlanHelper;
import fitBut.utils.FBConstants;
import fitBut.utils.Point;
import fitBut.utils.exceptions.ShouldNeverHappen;

import java.util.*;

/**
 * Retrieves block from map or dispenser
 */
public class FBGoalHoard extends FBFloodCheck {
    @SuppressWarnings("unused")
    private static final String TAG = "FBGoalHoard";
    private boolean hasTask;
    private BlockType requiredBlock;

    @Override
    boolean eachUnUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        return false;
    }

    @Override
    PlanCell getInitCell(FBAgent agent) {
        return new PlanCell(agent.getLatestPosition());
    }

    @Override
    protected boolean continueFailCheck(PlanCell cell) {
        return false;
    }

    @Override
    protected boolean mapBasedCheckFail(FBAgent agent, FBMapPlain map) {
        return !map.hasBlockOrDispenser();
    }

    @Override
    boolean preCheckFail(FBAgent agent) {
        hasTask = agent.getAgentInfo().getAcceptedTask() != null;
        if (hasTask) {
            requiredBlock = getCorrectBlockForTask(agent); // agent already accepted task
        }
        return agent.getBody().getList().size() >= FBConstants.HOARD_LIMIT;
    }

    @Override
    FBMapPlain goalMap(FBAgent agent) {
        Set<Point> body = agent.getBody().getShiftedList(agent.getLatestPosition());
        return agent.getMap().getMapSnapshot(0, agent.getLatestPosition(), body);
    }

    @Override
    boolean eachUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        //HorseRider.inquire(TAG, "makeGoals: " + agent.getName() + " field at: " + cell.getAt() + " without obstacle");// pre\n"+
        for (Point neighbour : PlanHelper.generateDirections(cell.getAt(), 0)) { // check all four side cells
            if (eachNeighbour(agent, map, cell, neighbour)) return true;
        }
        return eachNeighbour(agent, map, cell, cell.getAt().sum(new Point(0, 1)));
    }

    private boolean eachNeighbour(FBAgent agent, FBMapPlain map, PlanCell cell, Point neighbour) {
        FBBlockObject block = map.getBlockObjectAt(neighbour);

        if (block != null && block.notAttached() && doesNotHaveNeighbour(map, neighbour)) { // free block
            if (!hasTask || requiredBlock.equals(block.getBlockType())) { // if agent has task is it correct block
                // found a block free for taking
                plan = cell.getPlan();
                plan.appendAction(new FBAttach(neighbour.diff(cell.getAt()).getDirection()));
                return true;
            }
        }
        FBDispenserObject dispenserObject = map.getDispenserObjectAt(neighbour);
        if (dispenserObject != null && map.isTraversableAt(neighbour)) { // found a dispenser with space on it
            if (!hasTask || requiredBlock.equals(dispenserObject.getDispenserType())) { // if agent has task is it correct block
                Point vector = neighbour.diff(cell.getAt()).rotate(cell.getHeading().rotationTo(Direction.N));
                Set<Point> relativeBody = agent.getBody().getList();
                if (!relativeBody.contains(vector)) {
                    plan = cell.getPlan();
                    plan.appendAction(new FBRequest(neighbour.diff(cell.getAt()).getDirection()));
                    return true;
                }
            }
        }
        return false;
    }

    private BlockType getCorrectBlockForTask(FBAgent agent) {
        FBTask task = agent.getTasks().get(agent.getAgentInfo().getAcceptedTask());
        Point firstBlockPos = new Point(0, 1);
        for (Rotation rotation : Rotation.fourDirections()) {  // find first task block
            Point firstBlockPosR = firstBlockPos.getRotated(rotation);
            if (task.getTaskBody().containsKey(firstBlockPosR)) {   // first block found
                return task.getTaskBody().get(firstBlockPosR);
            }
        }
        throw new ShouldNeverHappen(TAG + " getCorrectBlockForTask: " + agent.getName() + " no first block for task found! ");
    }

    private boolean doesNotHaveNeighbour(FBMapPlain map, Point blockPoint) {
        for (Point neighbour : PlanHelper.generateDirections(blockPoint, 0)) {
            if (map.getNodeFirstByType(neighbour, FBObjectType.__FBEntity_Friend) != null //||
                //map.getNodeFirstByType(neighbour, FBObjectType.__FBEntity_Enemy) != null ||
                //map.getNodeFirstByType(neighbour, FBObjectType.__FBAgent) != null ||
                //map.getNodeFirstByType(neighbour, FBObjectType.__FBBlock) != null
            ) {
                return false;
            }
        }
        return true;
    }

    public FBGoalHoard() {
    }
}
