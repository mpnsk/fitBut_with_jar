package fitBut.fbReasoningModule.fbGoals.fbMultiGoals;


import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbReasoningModule.fbGoals.FBGoalRoam;
import fitBut.fbReasoningModule.fbGoals.fbMultiGoals.utils.PlanningStruct;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;

import java.util.HashMap;

/**
 * generates efficient paths for exploring
 * WARNING: ignores future movement of other agents
 */
public class FBGoalExplore extends FBFloodMultiCheck {
    private static final String TAG = "FBGoalExplore";
    private HashMap<Point, FBAgent> targetLocations;


    @Override
    protected boolean eachNonUsableField(PlanningStruct planningStruct, PlanCell cell, FBMapPlain snapshotMap) {
        Rotation rotation = Direction.N.rotationTo(cell.getHeading());

        if (snapshotMap.collidesWithUnknown(cell.getAt(), planningStruct.agent.getBody().getList(), rotation)) {  // field unknown
            boolean positionQueued = false;
            for (Point target : targetLocations.keySet()) {
                if (cell.getAt().limitedDistance(target) < planningStruct.agent.getSimInfo().getVision()/2) {
                    positionQueued = true;
                    /*HorseRider.inquire(TAG, "makeGoals: " + planningStruct.agent.getName() + " unknown field " +
                            cell.getAt() + " too close to " + target + " (" + targetLocations.get(target) + ")");
                    */
                    break;
                }
            }
            if (!positionQueued) { // found new interesting location
                HorseRider.inquire(TAG, "makeGoals: " + planningStruct.agent.getName() + " unknown field found at: " + cell.getAt() + " plan: " + cell.getPlan());
                planningStruct.agentGoal = new FBGoalRoam(cell.getPlan());
                targetLocations.put(cell.getAt(), planningStruct.agent);
                planningStruct.open.clear();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void preSet() {
        targetLocations = new HashMap<>();
    }

    @Override
    boolean preCheckFail(FBAgent agent) {
        return false;
    }

    @Override
    boolean eachUsableField(PlanningStruct planningStruct, PlanCell cell, FBMapPlain map) {
        return false;
    }

    public FBGoalExplore() {
    }
}
