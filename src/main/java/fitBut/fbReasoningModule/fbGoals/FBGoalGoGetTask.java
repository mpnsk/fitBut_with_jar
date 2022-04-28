package fitBut.fbReasoningModule.fbGoals;

import fitBut.agents.FBAgent;
import fitBut.fbActions.FBAccept;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.fbReasoningModule.fbGoals.utils.TaskMatch;
import fitBut.fbReasoningModule.fbPlans.FBPlan;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;

import java.util.*;

import static fitBut.fbReasoningModule.fbGoals.utils.TaskMatch.taskMatch;

/**
 * @author : Vaclav Uhlir
 * @since : 9.10.2019
 **/
public class FBGoalGoGetTask extends FBFloodCheck {
    private static final String TAG = "FBGoalGoGetTask";
    private String bestTask = null;


    public FBGoalGoGetTask() {
        HorseRider.challenge(TAG, "FBGoalGoGetTask plan");
    }

    @Override
    protected boolean continueFailCheck(PlanCell cell) {
        return false;
    }

    @Override
    protected boolean mapBasedCheckFail(FBAgent agent, FBMapPlain map) {
        return map.doesNotHaveTakBoard();
    }

    @Override
    boolean preCheckFail(FBAgent agent) {
        //return agent.getTasks().isEmpty();
        if (agent.getAgentInfo().getAcceptedTask() == null && //) { // does not have task
                !agent.getBody().getBodyBlocks().isEmpty()) {  //and does have body
            //lets find the most suitable
            ArrayList<TaskMatch.TaskMatchStructure> taskCompleteness = new ArrayList<>();
            for (FBTask task : agent.getTasks().values()) {// get task completeness
                taskCompleteness.addAll(taskMatch(task, agent));
            }
            if (!taskCompleteness.isEmpty()) {
                taskCompleteness.sort(Comparator.comparingInt(TaskMatch.TaskMatchStructure::getReward).reversed());
                taskCompleteness.sort(Comparator.comparingDouble(TaskMatch.TaskMatchStructure::getHitRatio).reversed());
                bestTask = taskCompleteness.get(0).getTask().getName();
                HorseRider.challenge(TAG, "preCheckFail: " + agent + " selecting task " + bestTask + "\n" +
                        "from " + taskCompleteness.toString());
                return false; // do not fail and run algorithm
            } else {
                HorseRider.challenge(TAG, "preCheckFail: " + agent + " suitable no tasks");
            }
        } else {
            HorseRider.challenge(TAG, "preCheckFail: " + agent + " fail on precons: " +
                    "no active task: "+(agent.getAgentInfo().getAcceptedTask() == null) + ", has some body: " +
                    !agent.getBody().getBodyBlocks().isEmpty());
        }
        return true; //do not run search
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
        //return agent.getMap().getMapSnapshot(0, agentPosition(agent), body);
        return agent.getLocalMap().getMapSnapshot(0, agentPosition(agent), body); //todo test local map
    }

    @Override
    Point agentPosition(FBAgent agent) {
        return agent.getLatestLocalPosition();
    }

    @Override
    boolean eachUsableField(FBAgent agent, PlanCell cell, FBMapPlain map) {
        //FBCellObject goal = map.getNodeFirstByType(cell.getAt(),FBObjectType.__FBTaskBoard);
        Point goalPoint = isNearTaskBoard(cell, map);

        if (goalPoint != null) { //heading towards TaskBoard
            HorseRider.challenge(TAG, "eachUsableField: " + agent + " " + cell.getAt() + " " + cell.getHeading() + " to: " + goalPoint + " (distance: " + goalPoint.distance(cell.getAt()) + ")");
            if (cell.getPlan().size() > 0) { //far from TaskBoard
                //found goal
                plan = cell.getPlan();
                return true;
            } else if (agent.getTasks().isEmpty()) {  // no tasks to lock in
                return false;
            } else {
                plan = new FBPlan();
                if (bestTask != null) {
                    plan.appendAction(new FBAccept(bestTask));
                } else {
                    String randTask = random(agent.getTasks().values()).getName();
                    HorseRider.warn(TAG, "eachUsableField: " + agent + " task not preselected - selecting random " + randTask);
                    plan.appendAction(new FBAccept(randTask));
                }
                return true;
            }
        }
        return false;
    }

    @Deprecated
    private static <T> T random(Collection<T> coll) {
        int num = (int) (Math.random() * coll.size());
        for (T t : coll) if (--num < 0) return t;
        throw new AssertionError();
    }

    private Point isNearTaskBoard(PlanCell cell, FBMapPlain map) {
        Point goalPoint = null;
        for (Point taskBoard : map.getTaskBoards()) {
            if (taskBoard.limitedDistance(cell.getAt()) <= 2) {
                goalPoint = taskBoard;
            }
        }
        return goalPoint;
    }
}
