package fitBut.fbActions;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbEnvironment.FBBody;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.fbReasoningModule.fbGoals.FBGoalSplit;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;

import java.util.HashSet;

public class FBConnect extends FBAction {

    private static final String TAG = "FBConnect";
    private final Point pointTo;
    private final FBAgent partner;
    private final Point pointFrom;
    private final String partnerName;
    private final Point disconnectFrom;
    private final Point disconnectTo;
    private final String taskName;

    @Override
    public Action getEisAction() {
        return new Action("connect", new Identifier(partnerName), new Numeral(pointFrom.x), new Numeral(pointFrom.y));
    }

    @Override
    public void succeededEffect(FBAgent agent, int step) {
        HorseRider.inquire(TAG, "succeededEffect: " + agent +" connecting "+ pointFrom + " to " + pointTo);
        Point agentPosition = agent.getLatestPosition();
        Point attachingFrom = agentPosition.sum(pointFrom);
        Point attachingTo = agentPosition.sum(pointTo);


        FBBlockObject fromBlockObject = agent.getMap().getBlockObjectAt(attachingFrom);
        FBBlockObject toBlockObject = agent.getMap().getBlockObjectAt(attachingTo);

        if (fromBlockObject != null && toBlockObject != null) {
            HashSet<Point> list = new HashSet<>();
            list.add(attachingTo.diff(partner.getLatestPosition()));
            FBBody partnerBody = partner.getBody().getClone();
            attachLinkedNeighbours(agent, partnerBody, attachingFrom, list);
            setDisconnectOutsideOfTask(agent, step);
        } else {
            HorseRider.yell(TAG, "succeededEffect: connecting blocks not present! " + fromBlockObject + " " + toBlockObject);
        }
    }

    private void setDisconnectOutsideOfTask(FBAgent agent, int step) {
        agent.addOrder(new FBGoalSplit(disconnectFrom, disconnectTo, taskName), step);
        agent.getGroup().addTaskWorkedOnIndex(step, taskName, 1);
        agent.setBusy(step);
    }

    private void attachLinkedNeighbours(FBAgent agent, FBBody partnerBody, Point source, HashSet<Point> linked) {
        for (Point link : linked) {
            Point point = partner.getLatestPosition().sum(link);
            FBBlockObject blockObject = agent.getMap().getBlockObjectAt(point);
            if (blockObject != null) {
                if (blockObject.isNotAttachedTo(agent)) { //attach partner blocks
                    blockObject.addAttachedTo(agent);
                    HorseRider.inquire(TAG, "attachLinkedNeighbours: " + agent + " linking " + source.diff(agent.getLatestPosition()) + " " + point.diff(agent.getLatestPosition()) +
                            " (" + source + " " + point + ")");
                    agent.getBody().addCell(source.diff(agent.getLatestPosition()), point.diff(agent.getLatestPosition()), (FBBlockObject) blockObject.getClone());
                    attachLinkedNeighbours(agent, partnerBody, point, partnerBody.getLinked(point.diff(partner.getLatestPosition())));
                }
            }
        }
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
            case FAILED_PARAMETER:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED_PARAMETER action: " + fbAgent + "First parameter is not an agent of the same team OR x and y cannot be parsed to valid integers.");
                break;
            case FAILED_PARTNER:
                HorseRider.inquire(TAG, "getAgentActionFeedback: FAILED_PARTNER action: " + fbAgent + "The partner's action is not connect OR failed randomly OR has wrong parameters.");
                break;
            case FAILED_TARGET:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED_TARGET action: " + fbAgent + "At least one of the specified blocks is not at the given position or not attached to the agent or already attached to the other agent.");
                break;
            case FAILED:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " The given positions are too far apart OR one agent is already attached to the other (or through other blocks), or connecting both blocks would violate the size limit for connected structures.");
                fbAgent.addOrder(new FBGoalSplit(disconnectFrom, disconnectTo, taskName), step);
                break;
            default:
                HorseRider.yell(TAG, "getAgentActionFeedback: FAILED action: " + fbAgent + " " + lastActionResult);
                break;
        }
    }

    public FBConnect(FBAgent partner, Point point, Point pointTo, Point disconnectFrom, Point disconnectTo, String taskName) {
        //super(ei);
        this.pointFrom = point;
        this.pointTo = pointTo;
        partnerName = partner.getAgentInfo().getName();
        this.partner = partner;
        this.disconnectFrom = disconnectFrom;
        this.disconnectTo = disconnectTo;
        this.taskName = taskName;
    }

}


