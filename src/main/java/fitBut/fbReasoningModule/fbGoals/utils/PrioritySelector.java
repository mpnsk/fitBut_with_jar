package fitBut.fbReasoningModule.fbGoals.utils;

import fitBut.fbActions.FBSubmit;
import fitBut.fbReasoningModule.fbGoals.*;
import fitBut.utils.logging.HorseRider;

/**
 * @author : Vaclav Uhlir
 * @since : 06/10/2019
 **/

final public class PrioritySelector {
    private static final String TAG = "PrioritySelector";

    public static int getBasePriority(FBGoal goal) { // TODO: modify based on goal+action
        if (goal instanceof FBGoalDoNothing) {
            return 1000;
        } else if (goal instanceof FBGoalGoSubmit && goal.getAction() instanceof FBSubmit) {
            return 900;
        } else if (goal instanceof FBGoalSplit) {           // <--- More than movement actions
            return 700;
        } else if (goal instanceof FBGoalGoSubmit && goal.getPlan().size()<3) { // ignore dodge
            return 650;
        } else if (goal instanceof FBGoalDodge) {
            return 600;
        } else if (goal instanceof FBGoalGoSubmit) {
            return 500;
        } else if (goal instanceof FBGoalGoConnect) {
            return 200;
        } else if (goal instanceof FBGoalHamperEnemy && ((FBGoalHamperEnemy)goal).enemyHasBlockOrConsecutiveHit()) {
            return 175;
        } else if (goal instanceof FBGoalGoGetTask) {
            return 150;
        } else if (goal instanceof FBGoalHoard) {
            return 100;
        } else if (goal instanceof FBGoalRoam) {
            return 50;
        } else if (goal instanceof FBGoalHamperEnemy) {
            return 25;
        } else if (goal instanceof FBGoalGoNearSubmit) {
            return 20;
        } else if (goal instanceof FBGoalDig) {
            return 10;
        } else {
            HorseRider.yell(TAG, "getBasePriority: unset priority: " + goal);
            return 1;
        }
    }
}
