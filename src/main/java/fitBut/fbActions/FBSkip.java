package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.utils.Point;
import eis.iilang.Action;


public class FBSkip extends FBAction {
    @SuppressWarnings("unused")
    private static final String TAG = "FBSkip";

    @Override
    public Action getEisAction() {
        return new Action("skip");
    }

    @Override
    public void getAgentActionFeedback(ActionResult lastActionResult, FBAgent fbAgent, int step) {
    }

    public void succeededEffect(FBAgent agent, int step) {
    }

    @Override
    public FBSimAgent simulate(FBAgent agent) {
        return FBSimAgent.getNewSimAgent(agent, Point.zero(), Rotation.NULL);
    }

    public FBSkip() {
    }

}
