package fitBut.fbReasoningModule.fbGoals;


import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.utils.Point;

import java.util.Set;

/**
 * Tries to get out of clear events
 */
public class FBGoalDodge extends FBFloodCheck {
    @SuppressWarnings("unused")
    private static final String TAG = "FBGoalHoard";
    private final int step;

    @Override
    protected boolean continueFailCheck(PlanCell cell) {
        return false;
    }

    @Override
    protected boolean mapBasedCheckFail(FBAgent agent, FBMapPlain map) {
        return map.doesNotHaveClearEventsIn(agent.getBody().getShiftedList(agent.getLatestLocalPosition()));
    }

    @Override
    boolean preCheckFail(FBAgent agent) {
        return false;
    }

    @Override
    FBMapPlain goalMap(FBAgent agent) {
        Set<Point> body = agent.getBody().getShiftedList(agent.getLatestLocalPosition());
        return agent.getLocalMap().getMapSnapshot(this.step, agent.getLatestLocalPosition(), body);
    }

    @Override
    boolean eachUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        //HorseRider.inquire(TAG, "makeGoals: " + agent.getName() + " field at: " + cell.getAt() + " without obstacle");// pre\n"+
        if (map.doesNotHaveClearEventsIn(agent.getBody().getShiftedAndRotatedList(cell.getAt(), Direction.N.rotationTo(cell.getHeading())))) { //todo check if rotates correctly
            plan = cell.getPlan();
            return true;
        }
        return false;
    }

    @Override
    boolean eachUnUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        Rotation rotation = Direction.N.rotationTo(cell.getHeading());
        if (map.collidesWithUnknown(cell.getAt(), agent.getBody().getList(), rotation)) {  // field unknown
            plan = cell.getPlan(); //set only as backup
        }
        return false;
    }

    @Override
    PlanCell getInitCell(FBAgent agent) {
        return new PlanCell(agent.getLatestLocalPosition());
    }


    public FBGoalDodge(int step) {
        this.step = step;
    }
}
