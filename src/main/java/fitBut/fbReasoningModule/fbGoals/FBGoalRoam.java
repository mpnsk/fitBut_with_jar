package fitBut.fbReasoningModule.fbGoals;


import fitBut.fbReasoningModule.fbPlans.FBPlan;
import fitBut.agents.FBAgent;

import fitBut.utils.logging.HorseRider;

/**
 * enclosing class for multi-goal exploration
 */
public class FBGoalRoam extends FBGoal {
    private static final String TAG = "FBGoalRoam";

    @Deprecated
    public FBPlan makePlan(FBAgent agent) {
        HorseRider.yell(TAG, "makePlan: This does nothing! Use MultiGoalRoam. " + agent);
        return plan;
    }

    public FBGoalRoam(FBPlan plan) {
        this.plan = plan;
    }
}
