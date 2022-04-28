package fitBut.fbReasoningModule.fbGoals;


import fitBut.agents.FBAgent;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbReasoningModule.fbPlans.FBPlan;
import fitBut.utils.logging.HorseRider;

/**
 * enclosing class for multi-goal order of two agent connection
 */
public class FBGoalGoConnect extends FBGoal {
    private static final String TAG = "FBGoalGoConnect";
    private final FBTask task;

    @Deprecated
    public FBPlan makePlan(FBAgent agent) {
        HorseRider.yell(TAG, "makePlan: This does nothing! Use MultiGoalRoam. " + agent);
        return plan;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " for task: " + task.getName() + " Plan: " + plan;
    }

    public FBGoalGoConnect(FBPlan plan, FBTask task) {
        this.task = task;
        this.plan = plan;
    }
}
