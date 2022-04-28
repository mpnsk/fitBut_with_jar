package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.fbReasoningModule.fbGoals.FBGoalSplit;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class FBDetach extends FBAction {

    private static final String TAG = "FBDetach";
    private final Direction direction;
    private final String taskName;

    @Override
    public Action getEisAction() {
        return new Action("detach", new Identifier(direction.getText()));
    }

    @Override
    public void succeededEffect(FBAgent agent, int step) {
        HorseRider.inquire(TAG, "succeededEffect: " + agent + " uncouple " + direction.getVector() + "\n" + agent.getBody());
        agent.setBody(agent.getBody().getClone().disconnectBodyPart(Point.zero(), direction.getVector()));
    }

    @Override
    public FBSimAgent simulate(FBAgent agent) {
        return FBSimAgent.getNewSimAgent(agent, Point.zero(), Rotation.NULL);
    }


    @Override
    public void getAgentActionFeedback(ActionResult lastActionResult, FBAgent fbAgent, int step) {
        switch (lastActionResult) {
            case SUCCESS:
                succeededEffect(fbAgent, step);
                break;
            case FAILED_RANDOM:
                fbAgent.getGroup().addTaskWorkedOnIndex(step, taskName, 1);
                fbAgent.addOrder(new FBGoalSplit(Point.zero(), direction.getVector(), taskName), step); // todo: find more appropriate place for this
                break;
            case FAILED_PARAMETER:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED_PARAMETER action: " + fbAgent + " Parameter is not a direction. ");
                break;
            case FAILED_TARGET:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED_TARGET action: " + fbAgent + " There was no attachment to detach in the given direction. ");
                break;
            case FAILED: // partner disconnected
                HorseRider.inquire(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " There was a thing but not attached to the agent. ");
                succeededEffect(fbAgent, step);
                break;
            default:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " " + lastActionResult);
                break;
        }
    }

    public FBDetach(Direction direction, String taskName) {
        this.direction = direction;
        this.taskName = taskName;
    }

}
