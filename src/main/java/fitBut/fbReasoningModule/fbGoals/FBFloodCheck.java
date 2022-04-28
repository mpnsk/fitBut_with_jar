package fitBut.fbReasoningModule.fbGoals;


import fitBut.agents.FBAgent;
import fitBut.fbActions.FBRotate;
import fitBut.fbEnvironment.FBBody;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.fbReasoningModule.fbGoals.utils.PlanHelper;
import fitBut.fbReasoningModule.fbPlans.FBPlan;
import fitBut.utils.Point;
import fitBut.utils.PointAndDir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * implements flood search on agent current map
 * takes into account all currently not traversable blocks
 */
public abstract class FBFloodCheck extends FBGoal {
    @SuppressWarnings("unused")
    private static final String TAG = "FBFloodCheck";

    public FBPlan makePlan(FBAgent agent) {
        if (preCheckFail(agent)) return null;

        int preferredDirection = agent.getName().hashCode() % 8;

        PlanCell initCell = getInitCell(agent);

        // init open queue
        ArrayList<PlanCell> open = PlanHelper.getNewPlanMap();
        open.add(initCell);

        FBMapPlain map = goalMap(agent);

        if (mapBasedCheckFail(agent, map)) return null;

        //map.printMap();

        // close list
        HashMap<PointAndDir, PlanCell> indexed = new HashMap<>();
        indexed.put(new PointAndDir(initCell.getAt().getLimited(), Direction.N), initCell);

        generateStepsToOpen(preferredDirection, open, indexed, initCell);


        while (open.size() > 0) {
            PlanCell cell = open.remove(0);
            if (cell != null) {// if field exists

                if (continueFailCheck(cell)) return null;

                Rotation rotation = Direction.N.rotationTo(cell.getHeading());
                Set<Point> bodyList = agent.getBody().getList();
                if (cell.getLastAction() instanceof FBRotate) {
                    Rotation backRotation = ((FBRotate) cell.getLastAction()).getRotation().mirrored();
                    bodyList = FBBody.generateRotationBody(agent.getBody().getList(), backRotation);
                }
                if (map.isTraversableAt(cell.getAt(), bodyList, rotation)) {            // field without obstacle

                    if (eachUsableField(agent, cell, map)) return plan;
                    generateStepsToOpen(preferredDirection, open, indexed, cell);
                } else {
                    if (eachUnUsableField(agent, cell, map)) return plan;
                }

            }
        }

        //ran out of queue
        return plan; //return optional backup plan
    }

    void generateStepsToOpen(int preferredDirection, ArrayList<PlanCell> open, HashMap<PointAndDir, PlanCell> indexed, PlanCell cell) {
        PlanHelper.generateStepsToOpen(open, indexed, cell, preferredDirection);
    }


    Point agentPosition(FBAgent agent) {
        return agent.getLatestPosition();
    }

    abstract boolean eachUnUsableField(FBAgent agent, PlanCell cell, FBMapPlain map);

    abstract PlanCell getInitCell(FBAgent agent);

    protected abstract boolean continueFailCheck(PlanCell cell);

    protected abstract boolean mapBasedCheckFail(FBAgent agent, FBMapPlain map);

    /**
     * returns true if conditions for goal are not met
     *
     * @param agent agent to be checked
     * @return true for saving resources
     */
    abstract boolean preCheckFail(FBAgent agent);

    /**
     * map to be used for evaluation
     *
     * @param agent agent for map source
     * @return true if plan is set
     */
    abstract FBMapPlain goalMap(FBAgent agent);

    /**
     * evaluation of every accessible field
     *
     * @param agent agent for goal
     * @param cell  cell to be evaluated
     * @param map   map on evaluation
     * @return true if plan is set
     */
    abstract boolean eachUsableField(FBAgent agent, PlanCell cell, FBMapPlain map);

}
