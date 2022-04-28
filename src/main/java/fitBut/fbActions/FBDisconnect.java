package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.fbReasoningModule.fbGoals.FBGoalSplit;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;
import eis.iilang.Action;
import eis.iilang.Numeral;


public class FBDisconnect extends FBAction {
    private static final String TAG = "FBDisconnect";
    private final Point from;
    private final Point to;
    private final String taskName;


    public FBDisconnect(Point from, Point to, String taskName) {
        this.from = from;
        this.to = to;
        this.taskName = taskName;
    }

    @Override
    public Action getEisAction() {
        return new Action("disconnect",
                new Numeral(from.x), new Numeral(from.y),
                new Numeral(to.x), new Numeral(to.y));
    }

    @Override
    public void getAgentActionFeedback(ActionResult lastActionResult, FBAgent fbAgent, int step) {
        switch (lastActionResult) {
            case SUCCESS:
                succeededEffect(fbAgent, step);
                break;
            case FAILED_RANDOM:
                fbAgent.getGroup().addTaskWorkedOnIndex(step, taskName, 1);
                fbAgent.addOrder(new FBGoalSplit(from, to, taskName), step); // todo: find more appropriate place for this
                fbAgent.setBusy(step);
                break;
            case FAILED_PARAMETER:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED_PARAMETER action: " + fbAgent + " No valid integer coordinates given. ");
                break;
            case FAILED_TARGET: //partner disconnect?
                HorseRider.inquire(TAG, "getAgentActionFeedback: FAILED_TARGET action: " + fbAgent + " Target locations aren't attachments of the agent or not attached to each other directly. ");
                succeededEffect(fbAgent, step);
                break;
            default:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " " + lastActionResult);
                break;
        }
    }

    @Override
    public void succeededEffect(FBAgent agent, int step) {
        HorseRider.inquire(TAG, "succeededEffect: " + agent + " uncouple " + from + " " + to + "\n" + agent.getBody());
        agent.setBody(agent.getBody().getClone().disconnectBodyPart(from, to));
    }

    @Override
    public FBSimAgent simulate(FBAgent agent) {
        return FBSimAgent.getNewSimAgent(agent, Point.zero(), Rotation.NULL);
    }
}
