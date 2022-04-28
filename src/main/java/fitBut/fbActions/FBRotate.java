package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.FBBody;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.fbPerceptionModule.data.BlockType;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;
import eis.iilang.Action;
import eis.iilang.Identifier;

import java.util.Set;

public class FBRotate extends FBAction {

    private static final String TAG = "FBRotate";
    Rotation rotation;

    public FBRotate(Rotation rotation) {
        this.rotation = rotation;
    }

    @Override
    public void succeededEffect(FBAgent agent, int step) {
        agent.setBody(agent.getBody().getRotatedBody(rotation));
    }

    @Override
    public FBSimAgent simulate(FBAgent agent) {

        FBSimAgent newSimAgent = FBSimAgent.getNewSimAgent(agent, Point.zero(), getRotation());

        if (newSimAgent != null) {
            Set<Point> simBody = newSimAgent.getBody().getList();

            Set<Point> bodyList = FBBody.generateRotationBody(simBody, getRotation().mirrored());
            bodyList.remove(Point.zero());
            for (Point point : bodyList) {
                if (simBody.contains(point)) continue;
                FBBlockObject block = new FBBlockObject(new BlockType("br"), false);
                newSimAgent.getMap().addCellObject(newSimAgent.getLatestPosition().sum(point), block, newSimAgent.getLatestStep());
            }
        }

        return newSimAgent;
    }

    @Override
    public void getAgentActionFeedback(ActionResult lastActionResult, FBAgent fbAgent, int step) {
        switch (lastActionResult) {
            case SUCCESS:
                succeededEffect(fbAgent, step);
                break;
            case FAILED_RANDOM:
                break;
            case FAILED:
                //HorseRider.yell(TAG, "getAgentActionFeedback: "+FBAgent+" One of the things attached to the agent cannot rotate to its target position OR the agent is currently attached to another agent.");
                break;
            default:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " " + lastActionResult);
                break;
        }
    }

    @Override
    public void getAgentActionFeedbackReeval(ActionResult lastActionResult, FBAgent fbAgent, int step) {
        if (lastActionResult == ActionResult.FAILED) {
            if (checkIfBodyFoundBorder(fbAgent)) {
                hitBorder(fbAgent);
            } else {
                HorseRider.inquire(TAG, "getAgentActionFeedbackReeval: " + fbAgent + " One of the things attached to the agent cannot rotate to its target position OR the agent is currently attached to another agent.");
            }
        }
    }

    private void hitBorder(FBAgent fbAgent) {
        HorseRider.yell(TAG, "hitBorder: " + fbAgent + " ... well? How did we get here?");

    }

    private boolean checkIfBodyFoundBorder(FBAgent fbAgent) {
        if (fbAgent.getBody().getList().isEmpty()) return false;
        boolean hitUnknown = fbAgent.getLocalMap().isTraversableAt(
                fbAgent.getLatestLocalPosition(),
                FBBody.generateRotationBody(fbAgent.getBody().getList(), getRotation()),
                Rotation.NULL,
                fbAgent.getBody().getShiftedList(fbAgent.getLatestLocalPosition()));
        if (hitUnknown) {
            HorseRider.warn(TAG, "checkIfBodyFoundBorder: " + fbAgent + " hit something " + fbAgent.getBody());
        }
        return hitUnknown;
    }

    @Override
    public Action getEisAction() {
        return new Action("rotate", new Identifier(rotation.getText()));
    }


    public Rotation getRotation() {
        return rotation;
    }
}
