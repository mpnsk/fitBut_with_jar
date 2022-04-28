package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class FBRequest extends FBAction {

    private final Direction direction;
    private static final String TAG = "FBRequest";

    @Override
    public void getAgentActionFeedback(ActionResult lastActionResult, FBAgent fbAgent, int step) {
        switch (lastActionResult) {
            case SUCCESS:
                succeededEffect(fbAgent, step);
                break;
            case FAILED_RANDOM:
                break;
            default:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " " + lastActionResult);
                break;
        }
    }

    @Override
    public Action getEisAction() {
        return new Action("request", new Identifier(direction.getText()));
    }

    @Override
    public void succeededEffect(FBAgent agent, int step) {
        // no need to do anything
    }

    @Override
    public FBSimAgent simulate(FBAgent agent) {
        FBSimAgent newAgent = FBSimAgent.getNewSimAgent(agent, Point.zero(), Rotation.NULL);

        Point requestVector = getDirection().getVector();
        Point dispenserPosition = agent.getLatestPosition().sum(requestVector);
        if (agent.getMap().getDispenserObjectAt(dispenserPosition) == null) { //TODO: something somewhere went terribly wrong
            HorseRider.yell(TAG, "simulate: throw new ShouldNeverHappen " + agent.getName() + " nonexistent dispenser");
            //throw new ShouldNeverHappen(TAG + " simulate: " + agent.getName() + " nonexistent dispenser");
            return newAgent;
        }
        FBBlockObject block = new FBBlockObject(agent.getMap().getDispenserObjectAt(dispenserPosition).getDispenserType(), false);

        if (newAgent != null) {
            newAgent.getMap().addCellObject(dispenserPosition, block, newAgent.getLatestStep());
        }
        return newAgent;
    }


    public FBRequest(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
