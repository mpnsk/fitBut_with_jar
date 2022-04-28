package fitBut.agents;

import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.FBCells.objects.FBCellObject;
import fitBut.fbEnvironment.FBMap;
import fitBut.fbEnvironment.FBMapLayer;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbMultiagent.FBRegister;
import fitBut.fbReasoningModule.fbGoals.utils.PlanHelper;
import fitBut.utils.Point;
import fitBut.utils.exceptions.ShouldNeverHappen;
import fitBut.utils.logging.HorseRider;

/**
 * @author : Vaclav Uhlir
 * @since : 06/10/2019
 **/

public class FBSimAgent extends FBAgent {
    private static final String TAG = "FBSimAgent";
    private final int simStep;
    private final FBAgent originAgent;
    private Point simAgentPos;

    public FBSimAgent(FBAgent parent, int simStep) {
        super();
        this.name = parent.getName();
        this.map = new FBMapLayer(getName() + " sim " + simStep + " :", parent.getMap());
        this.simStep = simStep;
        this.originAgent = parent;
        this.simAgentPos = new Point(parent.getLatestPosition());
    }

    @Override
    public FBMap getMap() {
        return map;
    }

    @Override
    public String toString() {
        return "FBSimAgent{" +
                "simStep=" + simStep +
                ", originAgent=" + originAgent +
                ", simAgentPos=" + simAgentPos +
                '}';
    }

    /**
     * create new simulation agent based on parent
     *
     * @param agent      parent
     * @param moveVector move vector
     * @param rotation   rotation
     * @return new agent
     */
    public static FBSimAgent getNewSimAgent(FBAgent agent, Point moveVector, Rotation rotation) {
        FBSimAgent simAgent;
        int newStep;
        if (agent instanceof FBSimAgent) {
            newStep=((FBSimAgent) agent).getSimStep() + 1;
            simAgent = new FBSimAgent(((FBSimAgent) agent).getOriginAgent(), newStep);
        } else {
            newStep = agent.getLatestStep()+1;
            simAgent = new FBSimAgent(agent,newStep );
        }


        //agent block
        FBCellObject agentBlock = agent.getMap().getNodeFirstByType(agent.getLatestPosition(), FBObjectType.__FBAgent);
        Point newPos = agent.getLatestPosition().sum(moveVector).getLimited();

        simAgent.setMapPosition(newPos, simAgent.simStep);
        if (agentBlock == null) { //TODO: this should not happen -> debug
            for (Point neighbour : PlanHelper.generateDirections(agent.getLatestPosition(), 0)) { // check all four side cells
                FBCellObject testBlock = agent.getMap().getNodeFirstByType(neighbour, FBObjectType.__FBAgent);
                String source = agent.getMap().getCellSource(neighbour);
                HorseRider.warn(TAG, "getNewSimAgent: " + agent.getName() + " agents around: " + neighbour + " " +
                        testBlock + " from " + source);
            }
            HorseRider.yell(TAG, " getNewSimAgent: " + agent.getName() + " self agent not on " + agent.getLatestPosition() + "from: " + agent.getMap().getCellSource(agent.getLatestPosition()) + " in map:\n" + agent.getMap());
            FBRegister.GlobalVars.blackList(agent);
            //throw new ShouldNeverHappen(TAG + " FBAgent: " + agent.getName() + " does not have percepts of itself!");
            return null;
        }
        simAgent.getMap().addCellObject(newPos, agentBlock.getClone(),newStep );

        //body
        addLinkedBlocks(simAgent, agent, Point.zero(), newPos, rotation, newStep);
        //simAgent.setBody(agent.getBody().getRotatedBody(rotation));
        return simAgent;
    }

    @Override
    public int getLatestStep(){
        return simStep;
    }
    private int getSimStep() {
        return simStep;
    }

    private static void addLinkedBlocks(FBSimAgent simAgent, FBAgent agent, Point source, Point newPos, Rotation rotation, int newStep) {
        for (Point bodyPart : agent.getBody().getLinked(source)) {
            if (bodyPart.equals(Point.zero())) continue;
            if (simAgent.getBody().getList().contains(bodyPart)) continue;
            FBBlockObject bodyBlock = agent.getBody().getBodyBlocks().get(bodyPart);//.getBlockObjectAt(agent.getPosition().sum(bodyPart));
            if (bodyBlock == null) {
                throw new ShouldNeverHappen(TAG + " getNewSimAgent: " + agent.getName() + " bodyBlock null");
            }
            simAgent.getBody().addCell(source, bodyPart, bodyBlock);
            simAgent.getMap().addCellObject(newPos.sum(bodyPart.getRotated(rotation)), bodyBlock, newStep);
            addLinkedBlocks(simAgent, agent, bodyPart, newPos, rotation, newStep);
        }
    }

    @Override
    public Point getLatestPosition() {
        return simAgentPos;
    }

    @Override
    public Point getPosition(int step) {
        return simAgentPos;
    }

    @Override
    public void setMapPosition(Point pos, int step) {
        simAgentPos = pos;
    }

    /*@Override
    public int getStep() {
        return this.simStep;
    }*/

    private FBAgent getOriginAgent() {
        return originAgent;
    }

    public FBMapLayer getMapLayer() {
        return (FBMapLayer) this.map;
    }
}
