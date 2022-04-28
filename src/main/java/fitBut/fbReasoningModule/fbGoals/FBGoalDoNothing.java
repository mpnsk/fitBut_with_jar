package fitBut.fbReasoningModule.fbGoals;


import fitBut.agents.FBAgent;
import fitBut.fbActions.FBSkip;
import fitBut.fbReasoningModule.fbPlans.FBPlan;

/**
 * wrapper for skip plan
 */
public class FBGoalDoNothing extends FBGoal {
    @SuppressWarnings("unused")
    private static final String TAG = "FBGoalDoNothing";

    public FBPlan makePlan(FBAgent agent) {
        plan = new FBPlan();
        plan.appendAction(new FBSkip());
        return plan;
    }

    public FBGoalDoNothing() {
        makePlan(null);
    }
}
