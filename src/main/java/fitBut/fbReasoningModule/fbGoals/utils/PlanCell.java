package fitBut.fbReasoningModule.fbGoals.utils;

import fitBut.fbActions.FBAction;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbReasoningModule.fbPlans.FBPlan;

import fitBut.utils.Point;

/**
 * @author : Vaclav Uhlir
 * @since : 11.9.2019
 **/
public class PlanCell {

    @SuppressWarnings("unused")
    private static final String TAG = "PlanCell";
    private Point at;
    private FBPlan planToCell = new FBPlan();
    private Direction heading = Direction.N;
    private int step = 0;

    @Override
    public String toString(){
        return "PlantCell at "+at+" "+heading+" "+planToCell;
    }
    /**
     * new cell with at point
     *
     * @param point cell address
     */
    public PlanCell(Point point) {
        this.at = point;
    }

    public Point getAt() {
        return this.at;
    }

    void appendToActionPlan(FBAction action) {
        planToCell.appendAction(action);
    }

    public FBPlan getPlan() {
        return planToCell;
    }

    void copyPlanOf(FBPlan plan) {
        planToCell = plan.getCopy();
    }

    public Direction getHeading() {
        return heading;
    }

    void setHeading(Direction heading) {
        this.heading = heading;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getQueueValue() {
        int minStepsNeeded = 0;
        return step + Math.max(0, minStepsNeeded /2-5); //TODO: TMP workaround!!!!!!!!
    }

    public FBAction getLastAction() {
        if(getPlan()==null) return null;
        return getPlan().getLastAction();
    }
}
