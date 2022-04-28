package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.utils.logging.HorseRider;
import eis.iilang.Action;

public abstract class FBAction {
    private static final String TAG = "FBAction";

    public static String getActionClassFromString(String actionClass) {
        switch (actionClass) {
            case "move":
                return FBMove.class.toString();
            case "rotate":
                return FBRotate.class.toString();
            case "request":
                return FBRequest.class.toString();
            case "attach":
                return FBAttach.class.toString();
            case "detach":
                return FBDetach.class.toString();
            case "connect":
                return FBConnect.class.toString();
            case "disconnect":
                return FBDisconnect.class.toString();
            case "clear":
                return FBClear.class.toString();
            case "accept":
                return FBAccept.class.toString();
            case "submit":
                return FBSubmit.class.toString();
            case "skip":
                return FBSkip.class.toString();
            case "no_action":
                return "null";
            default:
                HorseRider.yell(TAG, "getActionClassFromString: UNKNOWN action class: " + actionClass);
                return "null";
        }
    }

    public abstract Action getEisAction();

    public abstract void succeededEffect(FBAgent agent, int step); // after suc. feedback

    public abstract FBSimAgent simulate(FBAgent agent);

    public abstract void getAgentActionFeedback(ActionResult lastActionResult, FBAgent fbAgent, int step);

    @Override
    public String toString() {
        return this.getEisAction().toProlog();
    }

    public void getAgentActionFeedbackReeval(ActionResult lastActionResult, FBAgent fbAgent, int step) {
    }
}
