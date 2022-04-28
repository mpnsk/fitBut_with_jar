package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.utils.logging.HorseRider;
import eis.iilang.Action;
import eis.iilang.Identifier;

import fitBut.utils.Point;



public class FBMove extends fitBut.fbActions.FBAction {
    private static final String TAG = "FBMove";
    private final Direction direction;


    public FBMove(Point correctPoint) {
        direction = Direction.getDirectionFromXY(correctPoint.x, correctPoint.y);
    }

    private Direction getPlannedDirection() {
        return direction;
    }

    @Override
    public Action getEisAction() {
        return new Action("move", new Identifier(direction.getText()));
    }

    @Override
    public void getAgentActionFeedback(ActionResult lastActionResult, FBAgent fbAgent, int step) {
        switch (lastActionResult) {
            case SUCCESS:
                succeededEffect(fbAgent, step);
                break;
            case TMP_OP_REVERSE:
                fbAgent.updateMapPosition(Point.zero().diff(direction.getVector()),step);
                break;
            case FAILED_FORBIDDEN:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED_FORBIDDEN -> hitBorder: " + fbAgent + " wait, what?!?");
                //hitBorder(fbAgent);
                break;
            case FAILED_RANDOM:
                break;
            case FAILED_PATH://content moved to after step

                break;
            default:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " " + lastActionResult);
                break;
        }
    }

    @Override
    public void getAgentActionFeedbackReeval(ActionResult lastActionResult, FBAgent fbAgent, int step) {
        if (lastActionResult == ActionResult.FAILED_PATH) {
            /*if (checkIfBodyFoundBorder(fbAgent)) {
                HorseRider.yell(TAG, "getAgentActionFeedbackReeval: FAILED_PATH -> hitBorder: " + fbAgent+ " wait, what?!?");
                //hitBorder(fbAgent);
            } else {*/
            HorseRider.challenge(TAG, "getAgentActionFeedbackReeval: FAILED action: " + fbAgent + " " + lastActionResult);
            //}
        }
    }


    @Override
    public void succeededEffect(FBAgent agent, int step) {
        agent.updateMapPosition(direction.getVector(),step);
    }

    @Override
    public FBSimAgent simulate(FBAgent agent) {

        Point moveVector = getPlannedDirection().getVector();

        return FBSimAgent.getNewSimAgent(agent, moveVector, Rotation.NULL);
    }


    public FBMove(Direction direction) {
        this.direction = direction;
    }

}
