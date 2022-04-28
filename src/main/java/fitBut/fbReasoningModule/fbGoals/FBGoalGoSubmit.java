package fitBut.fbReasoningModule.fbGoals;

import fitBut.agents.FBAgent;
import fitBut.fbActions.FBSubmit;
import fitBut.fbEnvironment.FBCells.objects.FBCellObject;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;

import java.util.Set;

import static fitBut.fbEnvironment.utils.Direction.N;

/**
 * @author : Vaclav Uhlir
 * @since : 9.10.2019
 **/
public class FBGoalGoSubmit extends FBFloodCheck {
    private static final String TAG = "FBGoalGoSubmit";
    private final FBTask task;
    private final Rotation rotation;
    private final int timeToEnd;

    public FBGoalGoSubmit(FBAgent agent, FBTask task, Rotation rotation, int step) {
        this.task = task;
        this.rotation = rotation;
        this.timeToEnd = task.getDeadline()-step;
        HorseRider.challenge(TAG, "FBGoalGoSubmit: "+agent+" make plan");
        this.makePlan(agent);
    }

    @Override
    protected boolean continueFailCheck(PlanCell cell) {
        return cell.getPlan().size()>=timeToEnd;
    }

    @Override
    protected boolean mapBasedCheckFail(FBAgent agent, FBMapPlain map) {
        return map.doesNotHaveGoal();
    }

    @Override
    boolean preCheckFail(FBAgent agent) {
        return false;
    }

    @Override
    boolean eachUnUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        return false;
    }

    @Override
    PlanCell getInitCell(FBAgent agent) {
        return new PlanCell(agent.getLatestPosition());
    }

    @Override
    FBMapPlain goalMap(FBAgent agent) {
        Set<Point> body = agent.getBody().getShiftedList(agent.getLatestPosition());
        return agent.getMap().getMapSnapshot(0, agent.getLatestPosition(), body);
    }

    @Override
    boolean eachUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        FBCellObject goal = map.getNodeFirstByType(cell.getAt(),FBObjectType.__FBGoal);
        if(goal!=null && cell.getHeading().rotationTo(N).equals(this.rotation
        )){
            HorseRider.challenge(TAG, "eachUsableField: "+agent.getName()+" "+cell.getAt()+cell.getHeading()+" "+goal);
            //found goal
            plan = cell.getPlan();
            plan.appendAction(new FBSubmit(task.getName()));
            return true;
        }
        return false;
    }
}
