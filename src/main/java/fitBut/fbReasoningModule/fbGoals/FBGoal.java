package fitBut.fbReasoningModule.fbGoals;

import fitBut.agents.FBAgent;
import fitBut.fbActions.FBAction;
import fitBut.fbEnvironment.FBMapLayer;
import fitBut.fbReasoningModule.fbPlans.FBPlan;


public abstract class FBGoal {

    @SuppressWarnings("unused")
    private static final String TAG = "FBGoal";

    protected FBPlan plan;

    public abstract FBPlan makePlan(FBAgent agent);

    public FBGoal() {
        plan = null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " Plan: " + plan;
    }

    public FBAction getAction() {
        if (plan != null) {
            return plan.getAction();
        }
        return null;
    }

    public FBMapLayer getFuture(FBAgent fbAgent) {
        return plan.getFuture(fbAgent);
    }

    public FBPlan getPlan() {
        return plan;
    }

    public void setPlan(FBPlan appendAction) {
        plan = appendAction;
    }
}
