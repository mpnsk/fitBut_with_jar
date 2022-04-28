package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.utils.Point;
import eis.iilang.Action;
import eis.iilang.Identifier;
import fitBut.utils.logging.HorseRider;

public class FBSubmit extends FBAction {

    @SuppressWarnings("unused")
    private static final String TAG = "FBSubmit";
    private String taskName;

    @Override
    public Action getEisAction() {
        return new Action("submit", new Identifier(taskName));
    }

    @Override
    public void succeededEffect(FBAgent agent, int step) {
        //something after submit? .. block will vanish on check
        agent.getAgentInfo().setAcceptedTask(null);
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
                break;
            case FAILED_TARGET:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED_TARGET action: " + fbAgent + " No active task could be associated with first parameter, or task has not been accepted by the agent. ");
                break;
            case FAILED:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " One or more of the requested blocks are missing OR the agent is not on a goal terrain. ");
                break;
            default:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " " + lastActionResult);
                break;
        }
    }

    public FBSubmit(String taskName) {
        this.taskName = taskName;
    }
}
