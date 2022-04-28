package fitBut.fbReasoningModule.fbGoals;

import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBCells.objects.FBCellObject;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;

import java.util.Set;

/**
 * @author : Vaclav Uhlir
 * @since : 9.10.2019
 **/
public class FBGoalGoNearSubmit extends FBFloodCheck {
    private static final String TAG = "FBGoalGoNearSubmit";


    public FBGoalGoNearSubmit() {
        HorseRider.challenge(TAG, "FBGoalGoNearSubmit plan");
    }

    @Override
    protected boolean continueFailCheck(PlanCell cell) {
        return false;
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
        return new PlanCell(agentPosition(agent));
    }

    @Override
    FBMapPlain goalMap(FBAgent agent) {
        Set<Point> body = agent.getBody().getShiftedList(agentPosition(agent));
        //agent.getMap().getMapSnapshot(0, agentPosition(agent), body);
        return agent.getLocalMap().getMapSnapshot(0, agent.getLatestLocalPosition(), body); //todo test local map
    }

    @Override
    Point agentPosition(FBAgent agent) {
        return agent.getLatestLocalPosition();
    }


    @Override
    boolean eachUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        FBCellObject goal = map.getNodeFirstByType(cell.getAt(),FBObjectType.__FBGoal);
        if(goal!=null){
            //HorseRider.challenge(TAG, "eachUsableField: "+agent.getName()+" "+cell.getAt()+" "+ cell.getHeading()+" to: "+goal);
            if(cell.getPlan().size()>10) {
                //found goal
                plan = cell.getPlan();
                for (int i = 0; i < 10; i++) {
                    plan.removeLastAction();
                }
                return true;
            }else{
                plan = null;
                return true;
            }
        }
        return false;
    }
}
