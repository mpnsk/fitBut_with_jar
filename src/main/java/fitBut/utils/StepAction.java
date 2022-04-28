package fitBut.utils;

import eis.iilang.Action;
import fitBut.fbReasoningModule.fbGoals.FBGoal;

public class StepAction {
    int step;
    Action action;
    FBGoal goal;

    public StepAction(int step, FBGoal goal) {
        this.step = step;
        if(goal!=null && goal.getAction() != null){
            action=goal.getAction().getEisAction();
        }
        this.goal = goal;
    }

    public Action getAction() {
        return action;
    }

    public int getStep() {
        return step;
    }

    @Override
    public String toString() {
        return "step=" + step +
                ", action=" + (action==null?"null":action.toProlog()) +
                '}';
    }

    public FBGoal getGoal() {
        return goal;
    }
}
