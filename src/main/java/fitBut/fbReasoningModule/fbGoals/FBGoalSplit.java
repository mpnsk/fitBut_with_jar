package fitBut.fbReasoningModule.fbGoals;

import fitBut.agents.FBAgent;
import fitBut.fbActions.FBDetach;
import fitBut.fbActions.FBDisconnect;
import fitBut.fbReasoningModule.fbPlans.FBPlan;
import fitBut.utils.Point;

/**
 * @author : Vaclav Uhlir
 * @since : 13.10.2019
 **/
public class FBGoalSplit extends FBGoal {
    private final Point disconnectFrom;
    private final Point disconnectTo;
    private final String taskName;

    public FBGoalSplit(Point disconnectFrom, Point disconnectTo, String taskName) {
        this.disconnectFrom = disconnectFrom;
        this.disconnectTo = disconnectTo;
        this.taskName = taskName;
        this.makePlan(null);
    }

    @Override
    public FBPlan makePlan(FBAgent agent) {
        if (disconnectFrom.equals(Point.zero())) {
            this.plan = new FBPlan().appendAction(new FBDetach(disconnectTo.diff(disconnectFrom).getDirection(), taskName));
        } else if (disconnectTo.equals(Point.zero())) {
            this.plan = new FBPlan().appendAction(new FBDetach(disconnectFrom.diff(disconnectTo).getDirection(), taskName));
        } else {
            this.plan = new FBPlan().appendAction(new FBDisconnect(disconnectFrom, disconnectTo, taskName));
        }
        return plan;
    }
}
