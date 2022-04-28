package fitBut.fbPerceptionModule;

import eis.iilang.*;
import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBMap;
import fitBut.fbPerceptionModule.data.BlockType;
import fitBut.utils.logging.HorseRider;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.fbEnvironment.FBCells.objects.*;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbPerceptionModule.data.AgentInfo;
import fitBut.fbPerceptionModule.data.SimInfo;

import fitBut.utils.Point;

import java.util.*;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
@SuppressWarnings("deprecation")
public class FBPerceptsProcessorV2 {

    private static final String TAG = "FBPerceptsProcessorV2";
    private SimInfo simInfo;
    private AgentInfo agentInfo;
    private Set<List<Parameter>> goals;
    private Set<List<Parameter>> obstacles;
    private Set<List<Parameter>> things;
    private Set<List<Parameter>> attached;
    private HashSet<Point> attachedIndex;

    public void processPercepts(Collection<Percept> percepts, String name) {
        things = new HashSet<>();
        obstacles = new HashSet<>();
        goals = new HashSet<>();
        attached = new HashSet<>();
        simInfo.dumpTasks();
        //HorseRider.inquire(TAG, "processPercepts: " + name + " " +
        //        "dump tasks:" + simInfo.getTaskList().keySet());

        //HorseRider.inquire(TAG, "processPercepts: " + name + " " +percepts.size() + " percepts:");
        //for (Percept percept : percepts) {

        //WARNING: ConcurrentModificationException workaround!
        synchronized(percepts) {
            for (Percept percept : percepts) {
                HorseRider.inquire(TAG, "processPercepts: " + name + " " +
                        String.format("percept: %s - %s ", percept.getName(), percept.getParameters()));
                switch (percept.getName()) {
                    //map objects
                    case "thing":
                        things.add(percept.getParameters());
                        break;
                    case "obstacle":
                        obstacles.add(percept.getParameters());
                        break;
                    case "goal":
                        goals.add(percept.getParameters());
                        break;
                    case "attached":
                        attached.add(percept.getParameters());
                        break;

                    // Agent Information
                    case "name":
                        agentInfo.setName(percept.getParameters().get(0).toProlog());
                        break;
                    case "vision":
                        simInfo.setVision(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        break;
                    case "energy":
                        agentInfo.setEnergy(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        break;
                    case "disabled":
                        agentInfo.setDisabled(percept.getParameters().get(0).toProlog().equals("true"));
                        break;
                    case "team":
                        simInfo.setTeam(percept.getParameters().get(0).toProlog());
                        break;
                    case "accepted":
                        String taskName = percept.getParameters().get(0).toProlog();
                        agentInfo.setAcceptedTask(taskName);
                        break;
                    // Sim information
                    case "steps":
                        simInfo.setSteps(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        break;
                    case "simStart":
                        simInfo.setSimStarted(true);
                        break;
                    case "requestAction":
                        simInfo.setRequestingAction(true);
                        break;
                    case "score":
                        simInfo.setScore(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        break;
                    case "step":
                        simInfo.setStep(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        break;
                    case "actionID":
                        simInfo.setActionID(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        break;
                    case "lastAction":
                        simInfo.setLastAction(percept.getParameters().get(0).toProlog()); //TODO: parse to enum?
                        break;
                    case "lastActionParams":
                        simInfo.setLastActionParams(percept.getParameters().get(0)); //TODO: parse to something useful?
                        break;
                    case "deadline":
                        simInfo.setDeadline(Long.parseLong(percept.getParameters().get(0).toProlog()));
                        break;
                    case "timestamp":
                        simInfo.setTimestamp(Long.parseLong(percept.getParameters().get(0).toProlog()));
                        break;
                    case "lastActionResult":
                        simInfo.setLastActionResult(ActionResult.fromString(percept.getParameters().get(0).toProlog()));
                        break;
                    case "teamSize":
                        simInfo.setTeamSize(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        break;
                    case "task":
                        processTask(percept.getParameters());
                        break;

                    default:
                        HorseRider.warn(TAG, "processPercepts: " + agentInfo.getName() + " " +
                                String.format("Skipped: %s - %s \n", percept.getName(), percept.getParameters()));

                }
            }
        }
        if (agentInfo.getAcceptedTask() != null) {
            //HorseRider.inquire(TAG, "processPercepts: " + name + " " +
            //        "accepted in?:" + simInfo.getTaskList().keySet());
            if (!simInfo.getTaskList().containsKey(agentInfo.getAcceptedTask())) {
                HorseRider.inquire(TAG, "processPercepts: " + name + " " +
                        String.format("ignoring accepted task: %s", agentInfo.getAcceptedTask()));
                agentInfo.setAcceptedTask(null);
            }
        }
    }

    public void runMapUpdate(FBMap map, FBAgent agent, int step) {
        Point pov = agent.getLocalPosition(step);
        map.insertEmptyVisionCells(simInfo.getVision(), pov, step);
        makeAttachedIndex();
        processThings(map, things, agent,step);
        processObstacles(map, obstacles, pov, step);
        processGoals(map, goals, pov, step);
        map.printMap();
    }

    public boolean runMapMovedCheckDisconfirmPosition(FBMap map, FBAgent agent, int step) {
        Point pov = agent.getLocalPosition(step);
        return !checkGoals(map, goals, pov) ||  //if false definitely not it
                !checkThings(map, things, agent,step);
    }

    private void makeAttachedIndex() {
        attachedIndex = new HashSet<>();
        for (List<Parameter> parameters : attached) {
            if (parameters.size() != 2) {
                HorseRider.yell(TAG, "makeAttachedIndex: " + agentInfo.getName() + " " +
                        "unknown format!: " + parameters);
            } else {
                attachedIndex.add(new Point(Integer.parseInt(parameters.get(0).toProlog()), Integer.parseInt(parameters.get(1).toProlog())));
            }
        }
    }

    private void processTask(List<Parameter> parameters) {
        if (parameters.size() != 4) {
            HorseRider.yell(TAG, "processTask: " + agentInfo.getName() + " " +
                    "unknown format!: " + parameters);
        } else {
            String taskName = parameters.get(0).toProlog();
            int taskDeadline = Integer.parseInt(parameters.get(1).toProlog());
            if (taskDeadline < simInfo.getPerceptStep()) return;
            int taskReward = Integer.parseInt(parameters.get(2).toProlog());
            Set<List<Parameter>> taskStructure = new HashSet<>();
            for (Parameter block : ((ParameterList) parameters.get(3))) {
                taskStructure.add(((Function) block).getParameters());
            }
            simInfo.addTask(new FBTask(taskName, taskDeadline, taskReward, processTaskBlocks(taskStructure)));
        }
    }

    private HashMap<Point, BlockType> processTaskBlocks(Set<List<Parameter>> blocks) {
        HashMap<Point, BlockType> taskStructure = new HashMap<>();
        for (List<Parameter> parameters : blocks) {
            if (parameters.size() != 3) {
                HorseRider.yell(TAG, "processTaskBlocks: " + agentInfo.getName() + " " +
                        "unknown format!: " + parameters);
            } else {
                Point position = new Point(Integer.parseInt(parameters.get(0).toProlog()), Integer.parseInt(parameters.get(1).toProlog()));
                taskStructure.put(position, simInfo.getBlockType(parameters.get(2).toProlog()));
                //structureMap.addCellObject(position, new FBBlockObject(simInfo.getBlockType(parameters.get(2).toProlog()), true), simInfo.getStep());
            }
        }
        return taskStructure;
    }

    private void processGoals(FBMap map, Set<List<Parameter>> obstacles, Point pov, int step) {
        for (List<Parameter> parameters : obstacles) {
            if (parameters.size() != 2) {
                HorseRider.yell(TAG, "processGoals: " + agentInfo.getName() + " " +
                        "unknown format!: " + parameters);
            } else {
                Point position = new Point(pov);
                //HorseRider.inquire(TAG, "processGoals: "+agentInfo.getName()+" goal at: "+
                //        parameters.get(0).toProlog()+"\t"+parameters.get(1).toProlog());
                position.translate(Integer.parseInt(parameters.get(0).toProlog()), Integer.parseInt(parameters.get(1).toProlog()));
                map.addCellObject(position, new FBOtherObject(FBObjectType.__FBGoal), step);
            }

        }
    }

    private boolean checkGoals(FBMap map, Set<List<Parameter>> obstacles, Point pov) {
        for (List<Parameter> parameters : obstacles) {
            if (parameters.size() != 2) {
                HorseRider.yell(TAG, "checkGoals: " + agentInfo.getName() + " " +
                        "unknown format!: " + parameters);
            } else {
                Point position = new Point(pov);
                position.translate(Integer.parseInt(parameters.get(0).toProlog()), Integer.parseInt(parameters.get(1).toProlog()));
                if (map.getNodeFirstByType(position, FBObjectType.__FBGoal) == null) {
                    HorseRider.warn(TAG, "checkGoals: " + agentInfo.getName() + " " +
                            "goal not found on: " + position);
                    return false;
                }
            }

        }
        return true;
    }

    private void processObstacles(FBMap map, Set<List<Parameter>> obstacles, Point pov, int step) {
        for (List<Parameter> parameters : obstacles) {
            if (parameters.size() != 2) {
                HorseRider.yell(TAG, "processObstacles: " + agentInfo.getName() + " " +
                        "unknown format!: " + parameters);
            } else {
                Point position = new Point(pov);
                //HorseRider.inquire(TAG, "processObstacles: "+agentInfo.getName()+" obstacle at: "+
                //        parameters.get(0).toProlog()+"\t"+parameters.get(1).toProlog());
                position.translate(Integer.parseInt(parameters.get(0).toProlog()), Integer.parseInt(parameters.get(1).toProlog()));
                map.addCellObject(position, new FBOtherObject(FBObjectType.__FBObstacle), step);
            }

        }
    }

    private void processThings(FBMap map, Set<List<Parameter>> things, FBAgent agent, int step) {
        Point pov = agent.getLocalPosition(step);
        for (List<Parameter> parameters : things) {
            if (parameters.size() != 4) {
                HorseRider.yell(TAG, "processThings: " + agentInfo.getName() + " " +
                        "unknown format!: " + parameters);
            } else {
                Point position = new Point(pov); // get agent position
                Point relative = new Point(Integer.parseInt(parameters.get(0).toProlog()), Integer.parseInt(parameters.get(1).toProlog()));
                position.translate(relative); //translate
                switch (parameters.get(2).toProlog()) {
                    case "marker":
                        map.addCellObject(position, new FBMarkerObject(parameters.get(3).toProlog()), step);
                        break;
                    case "dispenser":
                        map.addCellObject(position, new FBDispenserObject(simInfo.getBlockType(parameters.get(3).toProlog())), step);
                        break;
                    case "taskboard":
                        map.addCellObject(position, new FBTaskBoardObject(simInfo.getBlockType(parameters.get(3).toProlog())), step);
                        break;
                    case "entity":
                        String team = parameters.get(3).toProlog();
                        FBObjectType type;

                        if (!team.equals(simInfo.getTeam())) { // not our team
                            type = FBObjectType.__FBEntity_Enemy;
                        } else if (position.equals(pov)) { // our team same position as agent => agent?
                            type = FBObjectType.__FBAgent;
                        } else {
                            type = FBObjectType.__FBEntity_Friend;
                        }
                        FBEntityObject entity = new FBEntityObject(type, team);
                        map.addCellObject(position, entity, step);
                        if (type == FBObjectType.__FBAgent) {
                            entity.setName(agentInfo.getName());
                        }

                        break;
                    case "block":
                        map.addCellObject(position, new FBBlockObject(simInfo.getBlockType(parameters.get(3).toProlog()), attachedIndex.contains(relative)), step);
                        break;
                    default:
                        HorseRider.yell(TAG, "processThings: " + agentInfo.getName() + " unprocessed: " + parameters);

                }
                /*HorseRider.inquire(TAG, "processThings: " + agentInfo.getName() + " " + String.format("thing: [%d,%d]: %s (%s) \n",
                        Integer.parseInt(parameters.get(0).toProlog()),
                        Integer.parseInt(parameters.get(1).toProlog()),
                        parameters.get(2), // what
                        parameters.get(3))); //team
                    */
            }
        }
    }

    private boolean checkThings(FBMap map, Set<List<Parameter>> things, FBAgent agent, int step) {
        int numberOfDiscrepancies = 0;
        int numberOfConfirmed =0;
        Point pov = agent.getLocalPosition(step);
        for (List<Parameter> parameters : things) {
            if (parameters.size() != 4) {
                HorseRider.yell(TAG, "checkThings: " + agentInfo.getName() + " " +
                        "unknown format!: " + parameters);
            } else {
                Point position = new Point(pov); // get agent position
                Point relative = new Point(Integer.parseInt(parameters.get(0).toProlog()), Integer.parseInt(parameters.get(1).toProlog()));
                position.translate(relative); //translate
                switch (parameters.get(2).toProlog()) {
                    case "dispenser": //todo: confirm that dispensers do not move or disappear
                        //map.addCellObject(position, new FBDispenserObject(simInfo.getBlockType(parameters.get(3).toProlog())), step);
                        if (map.getNodeFirstByType(position, FBObjectType.__FBDispenser) == null ||
                                map.getDispenserObjectAt(position).getDispenserType() != simInfo.getBlockType(parameters.get(3).toProlog())) {
                            HorseRider.warn(TAG, "checkThings: " + agentInfo.getName() + " " +
                                    "dispenser not found or changed type on: " + position);
                            return false;
                        }
                        break;
                    case "taskboard": //todo: confirm that taskboards do not move or disappear
                        //map.addCellObject(position, new FBTaskBoardObject(simInfo.getBlockType(parameters.get(3).toProlog())), step);
                        if (map.getNodeFirstByType(position, FBObjectType.__FBTaskBoard) == null) {
                            HorseRider.warn(TAG, "checkThings: " + agentInfo.getName() + " " +
                                    "taskBoard not found on: " + position);
                            return false;
                        }
                        break;
                    case "entity":
                        /*String team = parameters.get(3).toProlog();
                        FBObjectType type;

                        if (!team.equals(agentInfo.getTeam())) { // not our team
                            type = FBObjectType.__FBEntity_Enemy;
                        } else if (position.equals(pov)) { // our team same position as agent => agent?
                            type = FBObjectType.__FBAgent;
                        } else {
                            type = FBObjectType.__FBEntity_Friend;
                        }
                        //map.addCellObject(position, entity, step);
                        if (map.getNodeFirstByType(position, type) == null) {
                            HorseRider.warn(TAG, "checkThings: " + agentInfo.getName() + " " + type +
                                    " not found on: " + position);
                            numberOfDiscrepancies++;
                        }
                        break;*/
                        continue;
                    case "block":
                        /*map.addCellObject(position, new FBBlockObject(simInfo.getBlockType(parameters.get(3).toProlog()), attachedIndex.contains(relative)), step);
                        if (map.getBlockObjectAt(position) == null ||
                                map.getBlockObjectAt(position).getBlockType() != simInfo.getBlockType(parameters.get(3).toProlog())) {
                            HorseRider.warn(TAG, "checkThings: " + agentInfo.getName() +
                                    " block " + simInfo.getBlockType(parameters.get(3).toProlog()) + " not found on: " + position);
                            numberOfDiscrepancies++;
                        }else{
                            numberOfConfirmed++;
                        }
                        break;*/

                       continue;
                    default:
                        HorseRider.yell(TAG, "processThings: " + agentInfo.getName() + " unprocessed: " + parameters);

                }
                /*HorseRider.inquire(TAG, "processThings: " + agentInfo.getName() + " " + String.format("thing: [%d,%d]: %s (%s) \n",
                        Integer.parseInt(parameters.get(0).toProlog()),
                        Integer.parseInt(parameters.get(1).toProlog()),
                        parameters.get(2), // what
                        parameters.get(3))); //team
                    */
            }
        }
        /*if (numberOfDiscrepancies > numberOfConfirmed) { //todo check again possible shift instead of constant!
            HorseRider.warn(TAG, "checkThings: " + agentInfo.getName() +
                    " found too many missing things: " + numberOfDiscrepancies);
            return false;
        }*/
        return true;
    }

    public void setAgentInfo(AgentInfo agentInfo) {
        this.agentInfo = agentInfo;
    }

    public void setSimInfo(SimInfo simInfo) {
        this.simInfo = simInfo;
    }

    public int thingsCount() {
        if (things == null) {
            return 0;
        } else {
            return things.size();
        }
    }

}
