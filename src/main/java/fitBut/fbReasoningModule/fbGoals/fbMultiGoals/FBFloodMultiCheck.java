package fitBut.fbReasoningModule.fbGoals.fbMultiGoals;


import fitBut.agents.FBAgent;
import fitBut.fbActions.FBRotate;
import fitBut.fbEnvironment.FBBody;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbReasoningModule.fbGoals.FBGoal;
import fitBut.fbReasoningModule.fbGoals.fbMultiGoals.utils.PlanningStruct;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.fbReasoningModule.fbGoals.utils.PlanHelper;
import fitBut.utils.FBConstants;
import fitBut.utils.Point;
import fitBut.utils.PointAndDir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static fitBut.utils.FBConstants.MAX_SIM_LOOP_MULTIPLIER;


public abstract class FBFloodMultiCheck extends FBMultiGoal {
    @SuppressWarnings("unused")
    private static final String TAG = "FBFloodMultiCheck";
    int stepLimit;

    public FBFloodMultiCheck(){
        if(Point.LoopLimit.isFoundX() && Point.LoopLimit.isFoundY()){
            stepLimit= (int) (MAX_SIM_LOOP_MULTIPLIER*(Point.LoopLimit.getLoopX()+Point.LoopLimit.getLoopY()));
        }else if(Point.LoopLimit.isFoundX()){
            stepLimit= (int) (MAX_SIM_LOOP_MULTIPLIER*Point.LoopLimit.getLoopX()*2);
        }else if(Point.LoopLimit.isFoundY()){
            stepLimit= (int) (MAX_SIM_LOOP_MULTIPLIER*Point.LoopLimit.getLoopY()*2);
        }else{
            stepLimit=FBConstants.MAX_SIM_MULTI_STEP;
        }
    }

    @Override
    public HashMap<FBAgent, FBGoal> makeGoals(Set<FBAgent> agents, FBMapPlain snapshotMap, int step) {
        // implements flood search on agent current map
        // takes into account all currently not traversable blocks

        preSet();

        HashMap<FBAgent, PlanningStruct> agentData = getFBAgentPlanningStructHashMap(agents,step);
        boolean allDone = false;
        int simulationStep = 0;
        // for all agents do step at a time
        while (!allDone) {
            allDone = true;
            for (FBAgent agent : agents) {
                ArrayList<PlanCell> open = agentData.get(agent).open;
                if (!open.isEmpty() && !quitCondition(open.get(0).getStep())) {
                    allDone = false;
                    while (!open.isEmpty() && open.get(0).getQueueValue() < simulationStep) {
                        PlanCell cell = open.remove(0);
                        if (cell != null) {
                            // if field traversable
                            Rotation rotation = Direction.N.rotationTo(cell.getHeading());
                            // snapshotMap.printMap();
                            Set<Point> bodyList = agent.getBody().getList();
                            if (cell.getLastAction() instanceof FBRotate) {
                                Rotation backRotation = ((FBRotate) cell.getLastAction()).getRotation().mirrored();
                                bodyList = FBBody.generateRotationBody(agent.getBody().getList(), backRotation);
                            }/**TODO:LIMITING stopped here**/
                            if (snapshotMap.isTraversableAt(cell.getAt(), bodyList, rotation, agentData.get(agent).bodyCollisionIgnoreList)) {            // field without obstacle
                                if (eachUsableField(agentData.get(agent), cell, snapshotMap)) break;
                                PlanHelper.generateStepsToOpen(open, agentData.get(agent).indexed, cell, agentData.get(agent).preferredDirection);
                            } else {
                                if (eachNonUsableField(agentData.get(agent), cell, snapshotMap)) break;
                            }
                        }
                    }
                    postAgentStep(agentData.get(agent));
                }
            }
            simulationStep++;
        }

        HashMap<FBAgent, FBGoal> agentPlans = new HashMap<>();
        for (FBAgent agent : agents) {
            agentPlans.put(agent, agentData.get(agent).agentGoal);
        }
        return agentPlans;
    }

    boolean quitCondition(int simulationStep) {
        return simulationStep>stepLimit;
    }

    void postAgentStep(PlanningStruct planningStruct) {
    }

    protected HashMap<FBAgent, PlanningStruct> getFBAgentPlanningStructHashMap(Set<FBAgent> agents, int step) {
        HashMap<FBAgent, PlanningStruct> agentData = new HashMap<>();

        for (FBAgent agent : agents) {
            int preferredDirection = agent.getName().hashCode() % 8;

            PlanCell initCell = new PlanCell(agent.getPosition(step));
            // init open queue
            ArrayList<PlanCell> open = PlanHelper.getNewPlanMap();
            //open.add(initCell); // init cell wont be checked
            // close list
            HashMap<PointAndDir, PlanCell> indexed = new HashMap<>();
            indexed.put(new PointAndDir(initCell.getAt(), Direction.N), initCell);
            PlanHelper.generateStepsToOpen(open, indexed, initCell, preferredDirection);
            PlanningStruct planningStruct = new PlanningStruct();
            planningStruct.agent = agent;
            planningStruct.agentGoal = null;
            planningStruct.open = open;
            planningStruct.indexed = indexed;
            planningStruct.preferredDirection = preferredDirection;
            planningStruct.bodyCollisionIgnoreList = agent.getBody().getShiftedList(agent.getPosition(step));
            agentData.put(agent, planningStruct);
        }
        return agentData;
    }

    protected abstract boolean eachNonUsableField(PlanningStruct planningStruct, PlanCell cell, FBMapPlain snapshotMap);

    protected abstract void preSet();

    /**
     * returns true if conditions for goal are not met
     *
     * @param agent agent to be checked
     * @return true for saving resources
     */
    @SuppressWarnings("unused")
    abstract boolean preCheckFail(FBAgent agent);

    /**
     * evaluation of every accessible field
     *
     * @param planningStruct agent for goal
     * @param cell           cell to be evaluated
     * @param map            map on evaluation
     * @return  true if action finishes cycle
     */
    abstract boolean eachUsableField(PlanningStruct planningStruct, PlanCell cell, FBMapPlain map);
}
