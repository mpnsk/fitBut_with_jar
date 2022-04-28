package fitBut.fbReasoningModule.fbGoals;


import fitBut.agents.FBAgent;
import fitBut.fbActions.FBClear;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbMultiagent.FBRegister;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.fbReasoningModule.fbGoals.utils.PlanHelper;
import fitBut.fbReasoningModule.fbPlans.FBPlan;
import fitBut.utils.Point;
import fitBut.utils.PointAndDir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Digs at closest reasonable obstacle
 */
public class FBGoalDig extends FBFloodCheck {
    @SuppressWarnings("unused")
    private static final String TAG = "FBGoalHoard";
    private Point initPos;

    @Override
    protected boolean continueFailCheck(PlanCell cell) {
        return (initPos.distance(cell.getAt()) > FBRegister.GlobalVars.getVision());
    }

    @Override
    protected boolean mapBasedCheckFail(FBAgent agent, FBMapPlain map) {
        return false;
    }

    @Override
    boolean preCheckFail(FBAgent agent) {
        return agent.getAgentInfo().getEnergy() <= FBRegister.GlobalVars.getDigEnergyReq() * FBRegister.GlobalVars.getConsecutiveHitsNeeded(); //checks current energy against requirement
    }

    @Override
    FBMapPlain goalMap(FBAgent agent) {
        Set<Point> body = agent.getBody().getShiftedList(agent.getLatestLocalPosition());
        return agent.getLocalMap().getMapSnapshot(-10, agent.getLatestLocalPosition(), body);
    }

    @Override
    boolean eachUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        return false;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    boolean eachUnUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        if (map.getNodeFirstByType(cell.getAt(), FBObjectType.__FBObstacle) != null) { //on obstacle
            Point vector = cell.getAt().diff(agent.getLatestLocalPosition());
            if (vector.size() <= 1) { //if dig is too close - dig diagonally
                return false;
            }

            plan = new FBPlan();
            plan.appendAction(new FBClear(vector, agent));
            /*
            plan = cell.getPlan();
            plan.removeLastAction();
            plan.removeLastAction();
            plan.removeLastAction();
            plan.removeLastAction();
            Point vector = cell.getAt().diff(agent.getLatestLocalPosition());
            if (vector.size() <= 1) { //if dig is too close - dig diagonally
                if (vector.x == 0) {
                    vector.x = vector.y;
                } else {
                    vector.y = vector.x;
                }
            }
            plan.appendAction(new FBClear(vector, agent));
*/
            return true;
        } /*else {
            plan = cell.getPlan();
            plan.removeLastAction();
            HashSet<Point> checked = new HashSet<>();
            checked.add(cell.getAt());
            if (digAtNeighbour(agent, cell.getAt(), map, checked)){
                return true;
            }else{
                plan = null;

            }
        }*/ return false;
    }

    @Override
    void generateStepsToOpen(int preferredDirection, ArrayList<PlanCell> open, HashMap<PointAndDir, PlanCell> indexed, PlanCell cell) {
        PlanHelper.generateStepsToOpen(open, indexed, cell, preferredDirection, false);
    }

    private boolean digAtNeighbour(FBAgent agent, Point center, FBMapPlain map, HashSet<Point> checked) {
        for (Point neighbour : PlanHelper.generateDirections(center, 0, false)) {
            if (checked.contains(neighbour)) continue;
            checked.add(neighbour);
            if (map.getNodeFirstByType(neighbour, FBObjectType.__FBObstacle) != null) {
                plan.appendAction(new FBClear(neighbour.diff(agent.getLatestLocalPosition()), agent));
                return true;
            }
        }
        return false;
    }

    @Override
    PlanCell getInitCell(FBAgent agent) {
        initPos = agent.getLatestLocalPosition();
        return new PlanCell(agent.getLatestLocalPosition());
    }


    public FBGoalDig() {
    }
}
