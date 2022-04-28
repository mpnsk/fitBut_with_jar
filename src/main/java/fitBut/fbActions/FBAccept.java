package fitBut.fbActions;

import eis.iilang.Action;
import eis.iilang.Identifier;
import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.utils.Point;
import fitBut.utils.exceptions.ShouldNeverHappen;
import fitBut.utils.logging.HorseRider;

public class FBAccept extends FBAction {

    @SuppressWarnings("unused")
    private static final String TAG = "FBAccept";
    private final String taskName;

    @Override
    public Action getEisAction() {
        return new Action("accept", new Identifier(taskName));
    }

    @Override
    public void succeededEffect(FBAgent agent, int step) {
        if (agent.getAgentInfo().getAcceptedTask() != null) {
            if (!agent.getAgentInfo().getAcceptedTask().equals(taskName)) {
                throw new ShouldNeverHappen(TAG + " succeededEffect: " + agent.getName() + " taskName: " + taskName + " != " + agent.getAgentInfo().getAcceptedTask());
            }
        } else {
            HorseRider.yell(TAG, "getAgentActionFeedback: SUCCESS action but no task assigned?: " + agent + " " + taskName);
        }
        //HorseRider.yell(TAG, "getAgentActionFeedback: SUCCESS action: " + agent + " " + taskName);
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
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED_TARGET action: " + fbAgent + " No task parameter given or no such task found. ");
                break;
            case FAILED_LOCATION:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED_LOCATION action: " + fbAgent + " The agent is not close to a task board. ");
                break;
            default:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " " + lastActionResult);
                break;
        }
    }

    public FBAccept(String taskName) {
        this.taskName = taskName;
    }
}
