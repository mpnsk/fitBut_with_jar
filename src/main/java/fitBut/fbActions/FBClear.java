package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbMultiagent.FBRegister;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.utils.Point;
import eis.iilang.Action;
import eis.iilang.Numeral;

public class FBClear extends FBAction {

    @SuppressWarnings("unused")
    private static final String TAG = "FBClear";
    private final Point point;
    private final int energyBefore;

    public FBClear(Point point, FBAgent agent) {
        this.point = point;
        this.energyBefore = agent.getAgentInfo().getEnergy();
    }

    @Override
    public FBSimAgent simulate(FBAgent agent) {
        return FBSimAgent.getNewSimAgent(agent, Point.zero(), Rotation.NULL);
    }

    @Override
    public Action getEisAction() {
        return new Action("clear", new Numeral(point.x), new Numeral(point.y));
    }

    @Override
    public void succeededEffect(FBAgent agent, int step) {
        FBRegister.GlobalVars.setDigEnergyReq(energyBefore-agent.getAgentInfo().getEnergy());
    }

    @Override
    public void getAgentActionFeedback(ActionResult lastActionResult, FBAgent fbAgent, int step) {
    }
}

