package fitBut.fbReasoningModule.fbGoals;


import fitBut.agents.FBAgent;
import fitBut.fbActions.FBClear;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.FBCells.objects.FBCellObject;
import fitBut.fbEnvironment.FBCells.objects.FBMarkerObject;
import fitBut.fbEnvironment.FBMap;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbEnvironment.utils.MarkerType;
import fitBut.fbMultiagent.FBRegister;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.fbReasoningModule.fbGoals.utils.PlanHelper;
import fitBut.fbReasoningModule.fbPlans.FBPlan;
import fitBut.utils.Point;
import fitBut.utils.PointAndDir;
import fitBut.utils.logging.HorseRider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Digs at closest reasonable obstacle
 */
public class FBGoalHamperEnemy extends FBFloodCheck {
    @SuppressWarnings("unused")
    private static final String TAG = "FBGoalHamperEnemy";
    private Point initPos;
    private Point targetCell;
    private boolean isConsecutive = false;
    private boolean enemyWithBlock = false;
    int hitCount = 1 ;
    private Point attackVector;

    public FBGoalHamperEnemy(FBGoalHamperEnemy lastAction, FBAgent agent) {
        plan = null;
        hitCount = lastAction.getHitCount()+1;
        if(agent.getAgentInfo().getEnergy() >= FBRegister.GlobalVars.getDigEnergyReq()) {
            FBMap map = agent.getLocalMap().getMapSnapshot(-1);
            Point targetCell = lastAction.getTarget();
            Point vector = lastAction.getAttackVector();
            FBMarkerObject marker = (FBMarkerObject) map.getNodeFirstByType(targetCell, FBObjectType.__FBMarker);
            if (!(marker != null && marker.getMarkerType()== MarkerType.CLEAR)) { //position was cleared
                FBRegister.GlobalVars.setConsecutiveHitsNeeded(hitCount);
                return;
            }
            if (map.getNodeFirstByType(targetCell, FBObjectType.__FBEntity_Enemy) != null) {
                hitCellAgain(agent, targetCell, vector);
            }
            for (Point neighbour : PlanHelper.generateDirections(targetCell, 0, false)) {
                if (map.getNodeFirstByType(neighbour, FBObjectType.__FBEntity_Enemy) != null) {
                    hitCellAgain(agent, targetCell, vector);
                }
                FBBlockObject block = map.getBlockObjectAt(neighbour);
                if (block!=null && !block.isNotAttachedTo(agent)) {
                    enemyWithBlock = true;
                    hitCellAgain(agent, targetCell, vector);
                }
                /*for (Point neighboursNeighbour : PlanHelper.generateDirections(neighbour, 0, false)) {
                    block = map.getBlockObjectAt(neighboursNeighbour);
                    if (block!=null && !block.isNotAttachedTo(agent)) {
                        enemyWithBlock = true;
                    }
                }*/
            }
        }
    }

    private int getHitCount() {
        return hitCount;
    }

    private void hitCellAgain(FBAgent agent, Point targetCell, Point vector) {
        this.targetCell = targetCell;
        this.attackVector = vector;
        plan = new FBPlan();
        plan.appendAction(new FBClear(vector, agent));
        isConsecutive = true;
    }

    private Point getTarget() {
        return targetCell;
    }

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
        return agent.getLocalMap().getMapSnapshot(-1, agent.getLatestLocalPosition(), body);
    }

    @Override
    boolean eachUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        return false;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    boolean eachUnUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        if (map.getNodeFirstByType(cell.getAt(), FBObjectType.__FBEntity_Enemy) != null) { //an enemy
            targetCell = cell.getAt();
            Point vector = targetCell.diff(agent.getLatestLocalPosition());
            if (vector.size() <= 1) { //if dig is too close - dig diagonally
                targetCell = cell.getAt().sum(vector).getLimited();
            }

            vector = targetCell.diff(agent.getLatestLocalPosition());
            this.attackVector = vector;
            plan = new FBPlan();
            plan.appendAction(new FBClear(vector, agent));

            if(agent.getName().equals("A5")){
                HorseRider.inform(TAG, "eachUnUsableField: debug me");
            }
            for (Point neighbour : PlanHelper.generateDirections(cell.getAt(), 0, false)) {
                FBBlockObject block = map.getBlockObjectAt(neighbour);
                if (block!=null && block.isNotAttachedTo(agent)) {
                    enemyWithBlock = true;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    void generateStepsToOpen(int preferredDirection, ArrayList<PlanCell> open, HashMap<PointAndDir, PlanCell> indexed, PlanCell cell) {
        PlanHelper.generateStepsToOpen(open, indexed, cell, preferredDirection, false);
    }

    @Override
    PlanCell getInitCell(FBAgent agent) {
        initPos = agent.getLatestLocalPosition();
        return new PlanCell(agent.getLatestLocalPosition());
    }


    public FBGoalHamperEnemy() {
    }

    public boolean enemyHasBlockOrConsecutiveHit() {
        return enemyWithBlock || isConsecutive;
    }

    public Point getAttackVector() {
        return attackVector;
    }
}
