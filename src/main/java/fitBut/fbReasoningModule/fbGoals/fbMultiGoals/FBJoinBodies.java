package fitBut.fbReasoningModule.fbGoals.fbMultiGoals;

import fitBut.agents.FBAgent;
import fitBut.fbActions.FBConnect;
import fitBut.fbActions.FBSkip;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbMultiagent.FBRegister;
import fitBut.fbReasoningModule.fbGoals.FBGoalGoConnect;
import fitBut.fbReasoningModule.fbGoals.fbMultiGoals.utils.JoinStructure;
import fitBut.fbReasoningModule.fbGoals.fbMultiGoals.utils.PlanningStruct;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.fbReasoningModule.fbGoals.utils.PlanHelper;
import fitBut.fbReasoningModule.fbGoals.utils.TaskMatch;
import fitBut.utils.FBConstants;
import fitBut.utils.Point;
import fitBut.utils.PointAndDir;
import fitBut.utils.logging.HorseRider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static fitBut.fbEnvironment.utils.Direction.N;

/**
 * @author : Vaclav Uhlir
 * @since : 10.10.2019
 **/
public class FBJoinBodies extends FBFloodMultiCheck {
    @SuppressWarnings("unused")
    private static final String TAG = "FBJoinBodies";
    private final TaskMatch.TaskMatchStructure baseTMS;
    private final TaskMatch.TaskMatchStructure connectingTMS;
    private final JoinStructure connectionPoint;
    private HashMap<TaskPlanningStruct, TaskPlanningStruct> crossIndex;
    private int taskTimeoutIn;

    FBJoinBodies(TaskMatch.TaskMatchStructure baseTMS, TaskMatch.TaskMatchStructure taskHit, JoinStructure connectionPoint) {
        this.baseTMS = baseTMS;
        this.connectingTMS = taskHit;
        this.connectionPoint = connectionPoint;
    }

    @Override
    protected HashMap<FBAgent, PlanningStruct> getFBAgentPlanningStructHashMap(Set<FBAgent> agents, int step) {
        this.taskTimeoutIn = baseTMS.getTask().getDeadline() - step;
        HashMap<FBAgent, PlanningStruct> agentData = new HashMap<>();
        TaskPlanningStruct planningStructure = getPlanningStructure(baseTMS);
        planningStructure.joinFrom = baseTMS.bodyEquivalentOf(connectionPoint.getMasterPoint());
        planningStructure.joinTo = baseTMS.bodyEquivalentOf(connectionPoint.getMasterPoint().sum(connectionPoint.getDirToSlave().getVector()));
        planningStructure.disconnectFrom = baseTMS.bodyEquivalentOf(connectionPoint.getSlaveLastPoint());
        planningStructure.disconnectTo = baseTMS.bodyEquivalentOf(connectionPoint.getSlaveLastPoint().sum(connectionPoint.getSlaveSeparationDir().getVector()));
        agentData.put(baseTMS.getAgent(), planningStructure);

        TaskPlanningStruct planningStructure1 = getPlanningStructure(connectingTMS);
        planningStructure1.joinFrom = connectingTMS.bodyEquivalentOf(connectionPoint.getMasterPoint().sum(connectionPoint.getDirToSlave().getVector()));
        planningStructure1.joinTo = connectingTMS.bodyEquivalentOf(connectionPoint.getMasterPoint());
        planningStructure1.disconnectFrom = connectingTMS.bodyEquivalentOf(connectionPoint.getSlaveLastPoint().sum(connectionPoint.getSlaveSeparationDir().getVector()));
        planningStructure1.disconnectTo = connectingTMS.bodyEquivalentOf(connectionPoint.getSlaveLastPoint());
        agentData.put(connectingTMS.getAgent(), planningStructure1);
        crossIndex = new HashMap<>();
        crossIndex.put(planningStructure, planningStructure1);
        crossIndex.put(planningStructure1, planningStructure);
        return agentData;
    }

    @Override
    boolean quitCondition(int simulationStep) {
        return simulationStep > FBConstants.MAX_SIM_JOIN_STEP || simulationStep > taskTimeoutIn - 1;
    }

    @Override
    boolean eachUsableField(PlanningStruct planningStruct, PlanCell cell, FBMapPlain map) {
        if(FBRegister.GlobalVars.isBlackListed(planningStruct.agent) || FBRegister.GlobalVars.isBlackListed(crossIndex.get(planningStruct).agent))return false;
        TaskPlanningStruct taskPlanningStruct = (TaskPlanningStruct) planningStruct;

        Point from = cell.getAt().sum(taskPlanningStruct.joinFrom.getRotated(N.rotationTo(cell.getHeading())));
        Point to = cell.getAt().sum(taskPlanningStruct.joinTo.getRotated(N.rotationTo(cell.getHeading())));
        taskPlanningStruct.addAchieved(from, to, cell);

        if (crossIndex.get(taskPlanningStruct).containsTo(from)) {
            if (crossIndex.get(taskPlanningStruct).getTo(from).containsKey(to)) {
                PlanCell otherCell = crossIndex.get(taskPlanningStruct).getTo(from).get(to);
                /*HorseRider.challenge(TAG, "eachUsableField: possible field found - from: "+from + " to: "+ to +
                        "\nfrom: "+taskPlanningStruct.agent + " to: "+crossIndex.get(taskPlanningStruct).agent+" as:"+
                        "\nfrom: "+cell+
                        "\nto:   "+otherCell);*/
                try {
                    if (!PlanHelper.checkIfCompatible(planningStruct.agent, cell.getPlan(), crossIndex.get(planningStruct).agent, otherCell.getPlan()))
                        return false;
                } catch (Exception e) {
                    HorseRider.yell(TAG, "eachUsableField: failed compatability " + planningStruct.agent + " " + crossIndex.get(planningStruct).agent);
                    FBRegister.GlobalVars.blackList(planningStruct.agent);
                    FBRegister.GlobalVars.blackList(crossIndex.get(planningStruct).agent);
                    return false;
                }
                /*HorseRider.challenge(TAG, "eachUsableField: field found !!!!!!!! - from: "+from + " to: "+ to +
                        "\nfrom: "+taskPlanningStruct.agent +taskPlanningStruct.agent.getBody()+
                        "\nto: "+crossIndex.get(taskPlanningStruct).agent+ crossIndex.get(taskPlanningStruct).agent.getBody()+
                        "\nas:"+
                        "\nfrom: "+cell+
                        "\nto:   "+otherCell);*/

                // we found joining point!
                //for this agent
                planningStruct.agentGoal = new FBGoalGoConnect(cell.getPlan(), taskPlanningStruct.taskMatch.getTask());
                planningStruct.agentGoal.getPlan().appendAction(new FBConnect(
                                crossIndex.get(planningStruct).agent,
                                (taskPlanningStruct.joinFrom).getRotated(N.rotationTo(cell.getHeading())),
                                (taskPlanningStruct.joinTo).getRotated(N.rotationTo(cell.getHeading())),
                                (taskPlanningStruct.disconnectFrom).getRotated(N.rotationTo(cell.getHeading())),
                                (taskPlanningStruct.disconnectTo).getRotated(N.rotationTo(cell.getHeading())),
                                taskPlanningStruct.taskMatch.getTask().getName()
                        )
                );
                planningStruct.open.clear();

                //other agent
                crossIndex.get(planningStruct).agentGoal = new FBGoalGoConnect(otherCell.getPlan(), taskPlanningStruct.taskMatch.getTask());
                for (int i = otherCell.getStep(); i < cell.getStep(); i++) {
                    crossIndex.get(planningStruct).agentGoal.getPlan().appendAction(new FBSkip()); // wait for other agent at meeting point
                }
                crossIndex.get(planningStruct).agentGoal.getPlan().appendAction(new FBConnect(
                                planningStruct.agent,
                                crossIndex.get(planningStruct).joinFrom.getRotated(N.rotationTo(otherCell.getHeading())),
                                crossIndex.get(planningStruct).joinTo.getRotated(N.rotationTo(otherCell.getHeading())),
                                crossIndex.get(planningStruct).disconnectFrom.getRotated(N.rotationTo(otherCell.getHeading())),
                                crossIndex.get(planningStruct).disconnectTo.getRotated(N.rotationTo(otherCell.getHeading())),
                                taskPlanningStruct.taskMatch.getTask().getName()
                        )
                );
                crossIndex.get(planningStruct).open.clear();
                return true;
            }
        }
        return false;
    }

    @Override
    void postAgentStep(PlanningStruct planningStruct) {
    }

    @Override
    protected boolean eachNonUsableField(PlanningStruct planningStruct, PlanCell cell, FBMapPlain snapshotMap) {
        return false;
    }

    @Override
    protected void preSet() {

    }

    @Override
    boolean preCheckFail(FBAgent agent) {
        return false;
    }


    private TaskPlanningStruct getPlanningStructure(TaskMatch.TaskMatchStructure taskMatchStructure) {
        int preferredDirection = taskMatchStructure.getAgent().getName().hashCode() % 8;

        PlanCell initCell = new PlanCell(taskMatchStructure.getAgent().getLatestPosition());
        // init open queue
        ArrayList<PlanCell> open = PlanHelper.getNewPlanMap();
        open.add(initCell);

        // close list
        HashMap<PointAndDir, PlanCell> indexed = new HashMap<>();
        indexed.put(new PointAndDir(initCell.getAt().getLimited(), N), initCell);
        //PlanHelper.generateStepsToOpen(open, indexed, initCell, preferredDirection);
        //AStarHelper.addIndexToOpen(open, aStarTarget);

        TaskPlanningStruct taskPlanningStruct = new TaskPlanningStruct();
        taskPlanningStruct.agent = taskMatchStructure.getAgent();
        taskPlanningStruct.taskMatch = taskMatchStructure;
        taskPlanningStruct.agentGoal = null;
        taskPlanningStruct.open = open;
        taskPlanningStruct.indexed = indexed;
        taskPlanningStruct.preferredDirection = preferredDirection;
        taskPlanningStruct.bodyCollisionIgnoreList = taskMatchStructure.getAgent().getBody().getShiftedList(taskMatchStructure.getAgent().getLatestPosition());
        return taskPlanningStruct;
    }

    static class TaskPlanningStruct extends PlanningStruct {
        Point disconnectTo;
        Point disconnectFrom;
        TaskMatch.TaskMatchStructure taskMatch;
        Point joinTo;
        Point joinFrom;
        HashMap<Point, HashMap<Point, PlanCell>> positions = new HashMap<>();

        void addAchieved(Point from, Point to, PlanCell cell) {
            positions.putIfAbsent(to, new HashMap<>());
            positions.get(to).putIfAbsent(from, cell);
        }

        boolean containsTo(Point fromOther) {
            return positions.containsKey(fromOther);
        }

        HashMap<Point, PlanCell> getTo(Point fromOther) {
            return positions.get(fromOther);
        }
    }
}
