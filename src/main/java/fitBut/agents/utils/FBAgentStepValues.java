package fitBut.agents.utils;

import fitBut.fbReasoningModule.fbGoals.FBGoal;
import fitBut.utils.FBConstants;
import fitBut.utils.Point;

import java.util.TreeMap;

public class FBAgentStepValues {
    private Point agentMapPos = new Point(0, 0);
    private FBGoal stepAction = null;
    private final TreeMap<Integer, FBGoal> goalPool = new TreeMap<>();
    private boolean groupDecisionsDone = false;
    private boolean agentDecisionsDone = false;
    private boolean multiAgentConnectionFlag = false;
    private boolean busy = false;
    private boolean groupSynced = false;
    private boolean inDecisionMaking = true;
    private FBGoal finalAction;
    private int decisionTimeReserve = FBConstants.TIME_RESERVE;

    @SuppressWarnings("CopyConstructorMissesField")
    public FBAgentStepValues(FBAgentStepValues prevStepOrDefault) {
        if(prevStepOrDefault!=null){
            this.agentMapPos = new Point(prevStepOrDefault.getAgentMapPos());
        }
    }

    public Point getAgentMapPos() {
        return agentMapPos;
    }

    public void setAgentMapPos(Point agentMapPos) {
        this.agentMapPos = agentMapPos;
    }

    public FBGoal getStepAction() {
        return stepAction;
    }

    public void setStepAction(FBGoal stepAction) {
        this.stepAction = stepAction;
    }

    public FBGoal getFinalAction() {
        return finalAction;
    }

    public void setFinalAction() {
        this.finalAction = stepAction;
    }

    public void setNotInDecisionMaking() {
        this.inDecisionMaking = false;
    }

    public boolean isAgentDecisionsDone() {
        return agentDecisionsDone;
    }

    public void setAgentDecisionsDone() {
        this.agentDecisionsDone = true;
    }

    public TreeMap<Integer, FBGoal> getGoalPool() {
        return goalPool;
    }

    public void setGroupDecisionsDone() {
        groupDecisionsDone = true;
    }

    public boolean isInDecisionMaking() {
        return inDecisionMaking;
    }

    public boolean isGroupDecisionsDone() {
        return groupDecisionsDone;
    }

    public void setMultiAgentConnectionFlag(boolean multiAgentConnectionFlag) {
        this.multiAgentConnectionFlag = multiAgentConnectionFlag;
    }

    public boolean getMultiAgentConnectionFlag() {
        return multiAgentConnectionFlag;
    }

    public void setGroupSynced(boolean groupSynced) {
        this.groupSynced = groupSynced;
    }

    public boolean isGroupSynced() {
        return groupSynced;
    }

    public void setBusy() {
        this.busy = true;
    }

    public boolean getBusy() {
        return busy;
    }

    public int getDecisionTimeReserve() {
        return decisionTimeReserve;
    }

    public void setDecisionTimeReserve(int remainder, int timeLimitDiff) {
        if(remainder<100){
            decisionTimeReserve = Math.min(
                    FBConstants.TIME_RESERVE_MAX,
                    Math.max(
                            FBConstants.TIME_RESERVE_MIN,
                            timeLimitDiff+FBConstants.TIME_RESERVE_STEP));
        }else if(remainder>200){
            decisionTimeReserve = Math.min(
                    FBConstants.TIME_RESERVE_MAX,
                    Math.max(
                            FBConstants.TIME_RESERVE_MIN,
                            timeLimitDiff-FBConstants.TIME_RESERVE_STEP));
        }else{
            decisionTimeReserve = timeLimitDiff;
        }
    }
}
