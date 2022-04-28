package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.FBCells.objects.FBCellObject;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class FBAttach extends fitBut.fbActions.FBAction {
    private static final String TAG = "FBAttach";
    private final Direction direction;

    @Override
    public void succeededEffect(FBAgent agent, int step) {
        Point direction = this.direction.directionToDelta();
        Point agentPosition = agent.getLocalPosition(step);
        Point attaching = agentPosition.sum(direction);
        FBCellObject block = agent.getLocalMap().getBlockObjectAt(attaching);
        if (block == null) {
            HorseRider.yell(TAG, "succeededEffect: " + agent.getName() + " attaching not visible object ?!? at " + attaching);
        } else {
            agent.getBody().addCell(Point.zero(), direction, (FBBlockObject) block.getClone());
        }
    }

    @Override
    public FBSimAgent simulate(FBAgent agent) {
        FBSimAgent newAgent = FBSimAgent.getNewSimAgent(agent, Point.zero(), Rotation.NULL);

        Point attachVector = getDirection().getVector();
        Point blockPos = agent.getLatestPosition().sum(attachVector);
        FBBlockObject block = ((FBBlockObject) agent.getMap().getBlockObjectAt(blockPos).getClone());
        if (newAgent != null) {
            newAgent.getBody().addCell(Point.zero(), attachVector, block);
            newAgent.getMap().addCellObject(blockPos, block, newAgent.getLatestStep());
        }
        return newAgent;
    }

    @Override
    public void getAgentActionFeedback(ActionResult lastActionResult, FBAgent fbAgent, int step) {
        //pass;
    }

    @Override
    public void getAgentActionFeedbackReeval(ActionResult lastActionResult, FBAgent fbAgent, int step) {
        switch (lastActionResult) {
            case SUCCESS:
                succeededEffect(fbAgent, step);
                break;
            case FAILED_RANDOM:
                break;
            case FAILED:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " The thing could not be attached because the agent already has too many things attached OR the thing is already attached to an agent of another team.");
                break;
            default:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " " + lastActionResult);
                break;
        }
    }

    @Override
    public Action getEisAction() {
        return new Action("attach", new Identifier(direction.getText()));
    }

    public FBAttach(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
