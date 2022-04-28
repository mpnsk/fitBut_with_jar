package fitBut.fbReasoningModule.fbGoals.fbMultiGoals;

import fitBut.agents.Agent;
import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbMultiagent.FBGroup;
import fitBut.fbMultiagent.FBRegister;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbPerceptionModule.data.BlockType;
import fitBut.fbReasoningModule.fbGoals.FBGoal;
import fitBut.fbReasoningModule.fbGoals.FBGoalGoSubmit;
import fitBut.fbReasoningModule.fbGoals.fbMultiGoals.utils.JoinStructure;
import fitBut.fbReasoningModule.fbGoals.utils.PlanHelper;
import fitBut.fbReasoningModule.fbGoals.utils.TaskMatch;
import fitBut.utils.FBConstants;
import fitBut.utils.Point;
import fitBut.utils.PointAndDir;
import fitBut.utils.logging.HorseRider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static fitBut.fbReasoningModule.fbGoals.utils.TaskMatch.completeness;

/**
 * @author : Vaclav Uhlir
 * @since : 8.10.2019
 **/
public class FBGoalAssembleTasks extends FBMultiGoal {
    private static final String TAG = "FBGoalAssembleTasks";
    private FBMapPlain snapshotMap;
    private ConcurrentHashMap<String, Integer> taskWorkedOnIndex;

    public FBGoalAssembleTasks(FBGroup fbGroup) {
        super();
        if (fbGroup == null) {
            HorseRider.warn(TAG, "FBGoalAssembleTasks: Skipping working index");
        } else {
            taskWorkedOnIndex = fbGroup.getTaskWorkedOnIndex();
        }
    }

    @Override
    public HashMap<FBAgent, FBGoal> makeGoals(Set<FBAgent> agents, FBMapPlain snapshotMap, int step) {
        HashMap<FBAgent, FBGoal> agentPlans = new HashMap<>();
        this.snapshotMap = snapshotMap;
        if (agents.isEmpty()) {
            return agentPlans;
        }

        // check if we have any tasks to fulfill
        HashMap<String, FBTask> simTasks = agents.iterator().next().getTasks();
        HashMap<String, FBTask> allTasks = new HashMap<>();
        for (String taskName : FBRegister.getAcceptedTasks()) {
            try {
                if (simTasks.get(taskName) != null) {
                    allTasks.put(taskName, simTasks.get(taskName));
                } else {
                    HorseRider.yell(TAG, "makeGoals " + agents + "Accepted Task " + taskName + " not in sim tasks " + "\n" +
                            "sim: " + simTasks.keySet() + "\n" +
                            "accepted:" + FBRegister.getAcceptedTasks());
                }
            } catch (NullPointerException e) {
                HorseRider.yell(TAG, "makeGoals " + agents + "Accepted Task " + taskName + " not in sim tasks " + "\n" +
                        "sim: " + simTasks.keySet() + "\n" +
                        "accepted:" + FBRegister.getAcceptedTasks());
            }
        }
        //HorseRider.warn(TAG, "makeGoals " + agents + "Accepted Tasks: "+allTasks.keySet());
        if (allTasks.isEmpty()) {
            return agentPlans;
        }
        HashMap<String, FBTask> tasks = filterTasks(agents, allTasks);
        //HashMap<String, FBTask> tasks = allTasks;
        //check if some agent is ready to submit and eval completeness
        ArrayList<TaskMatch.TaskMatchStructure> agentTaskAllPossibilities = getAgentTaskCompleteness(agents, agentPlans, tasks, step);
        HorseRider.inquire(TAG, "makeGoals: complenetess done");

        // Assemble
        agentTaskAllPossibilities.sort(Comparator.comparingInt(TaskMatch.TaskMatchStructure::getNameHash));
        agentTaskAllPossibilities.sort(Comparator.comparingInt(TaskMatch.TaskMatchStructure::getReward).reversed());
        agentTaskAllPossibilities.sort(Comparator.comparingDouble(TaskMatch.TaskMatchStructure::getHitRatio).reversed());


        ArrayList<TaskMatch.TaskMatchStructure> hitsMasters = new ArrayList<>();
        ArrayList<TaskMatch.TaskMatchStructure> hitsComplimentary = new ArrayList<>();
        for (TaskMatch.TaskMatchStructure hit : agentTaskAllPossibilities) {
            if (hit.isMaster()) {
                hitsMasters.add(hit);
            } else {
                hitsComplimentary.add(hit);
            }
        }
        HorseRider.challenge(TAG, "makeGoals: " + agents + " pre assemble masters: " + hitsMasters.size() + " slaves: " + hitsComplimentary);
        ArrayList<PlanOption> options = new ArrayList<>();

        //todo select based on some other value not just biggest hit
        //for (TaskMatch.TaskMatchStructure task : agentTaskAllPossibilities) { // for all matches
        for (TaskMatch.TaskMatchStructure hitMaster : hitsMasters) { // for all starting matches //TODO: this disables building from inner parts
            if (agentPlans.get(hitMaster.getAgent()) != null) continue; // agent has plan
            if (hitMaster.getAgent().getAgentInfo().getAcceptedTask() == null ||
                    !hitMaster.getAgent().getAgentInfo().getAcceptedTask().equals(hitMaster.getTask().getName()))
                continue; // agent does not have task


            /*if (task.getHits().contains(new Point(0, 1)) &&
                    task.bodyEquivalentOf(Point.zero()).equals(Point.zero())) {*/
                /*HorseRider.challenge(TAG, "makeGoals: " + agents +
                        "\n assemble find supplements for " + task.getTask() + task.getAgent());*/
            options.addAll(connectSupplementAgent(hitMaster, hitsComplimentary, agentPlans, step));
            //}

            if (checkIfDone(agents, agentPlans)) break; //we are done
        }
        //HorseRider.challenge(TAG, "makeGoals: " + agents + " mid assemble");

        //options done lets find best values //todo this gets best to assemble not best to submit :/
        options.sort(Comparator.comparingInt(PlanOption::getNameHash));
        options.sort(Comparator.comparingInt(PlanOption::getPlanSize));
        options.sort(Comparator.comparingDouble(PlanOption::getValue).reversed());
        for (PlanOption option : options) {
            boolean skip = false;
            if (taskWorkedOnIndex.containsKey(option.task)) continue;  // task is worked on by somebody else
            for (FBAgent agent : option.agents) {
                if (agentPlans.containsKey(agent)) {
                    skip = true; // agent has plan
                    break;
                }
            }
            if (skip) continue;
            /*HorseRider.challenge(TAG, "makeGoals: selected option:"+
                    "\nplan " + option.taskBase.getAgent().getBody() +
                    "\nwith " + option.taskConnecting.getAgent().getBody() +
                    "\nto " + option.task +
                    "\nas " + option.plans);*/

            taskWorkedOnIndex.put(option.task.getName(), option.plans.size());
            agentPlans.putAll(option.plans);
        }

        //HorseRider.challenge(TAG, "makeGoals: " + agents + " post assemble");
        return agentPlans;
    }

    private boolean checkIfDone(Set<FBAgent> agents, HashMap<FBAgent, FBGoal> agentPlans) {
        int goalsSet = 0;
        for (Map.Entry<FBAgent, FBGoal> entry : agentPlans.entrySet()) {
            FBGoal fbGoal = entry.getValue();
            if (fbGoal != null) goalsSet++;
        }
        return agents.size() == goalsSet;
    }

    private HashMap<String, FBTask> filterTasks(Set<FBAgent> agents, HashMap<String, FBTask> allTasks) {
        ArrayList<TaskInterest> taskInterests = new ArrayList<>();

        HashMap<BlockType, Integer> blocksAvailable = getAvailableBlocks(agents);
        for (FBTask task : allTasks.values()) {
            boolean neededBlocksAvailable = true;
            HashMap<BlockType, Integer> needed = task.getTypesNeeded();
            for (Map.Entry<BlockType, Integer> entry : needed.entrySet()) {
                BlockType blockType = entry.getKey();
                Integer integer = entry.getValue();
                if (blocksAvailable.getOrDefault(blockType, 0) < integer) {
                    HorseRider.challenge(TAG, "filterTasks: task " + task.getName() +
                            " agents " + agents +
                            " missing blocks: " + blockType +
                            " available: " + blocksAvailable +
                            " blocks needed: " + needed);
                    neededBlocksAvailable = false;
                }
            }
            if (!neededBlocksAvailable) {
                HorseRider.challenge(TAG, "filterTasks: task " + task.getName() + " trimmed (not enough blocks)");
                continue;
            }
            if (task.getReward() <= 0) {
                HorseRider.challenge(TAG, "filterTasks: task " + task.getName() + " trimmed (no reward) ");
                continue;
            }

            TaskInterest taskInterest = new TaskInterest();
            taskInterest.task = task;
            taskInterest.value = ((double) task.getReward()) / (task.getBodySize()) / (task.getBodySize()) / (task.getBodySize()); // TODO: ?smaller better?
            taskInterests.add(taskInterest);
        }
        taskInterests.sort(Comparator.comparingInt(TaskInterest::getNameHash));
        taskInterests.sort(Comparator.comparingInt(TaskInterest::getReward).reversed());
        taskInterests.sort(Comparator.comparingDouble(TaskInterest::getValue).reversed());
        HashMap<String, FBTask> filtered = new HashMap<>();
        for (int i = 0; i < Math.min(FBConstants.MAX_TASK_COUNT, taskInterests.size()); i++) { // TODO: make smarter limiter
            filtered.put(taskInterests.get(i).task.getName(), taskInterests.get(i).task);
        }
        if (filtered.size() != allTasks.size()) {
            HorseRider.inquire(TAG, "filterTasks: " + agents + "task trimming from " + allTasks.size() + " to " + filtered.size());
        }
        return filtered;
    }

    private HashMap<BlockType, Integer> getAvailableBlocks(Set<FBAgent> agents) {
        HashMap<BlockType, Integer> blocksAvailable = new HashMap<>();
        for (FBAgent agent : agents) {
            HashMap<BlockType, Integer> agentBlocks = agent.getBody().getAvailableBlocks();
            agentBlocks.forEach((blockType, count) ->
                    blocksAvailable.put(
                            blockType,
                            blocksAvailable.getOrDefault(blockType, 0) + count));
        }
        return blocksAvailable;
    }

    private ArrayList<TaskMatch.TaskMatchStructure> getAgentTaskCompleteness(Set<FBAgent> agents, HashMap<FBAgent, FBGoal> agentPlans, HashMap<String, FBTask> tasks, int step) {
        ArrayList<TaskMatch.TaskMatchStructure> agentTaskPossibilities = new ArrayList<>();
        for (FBAgent agent : agents) {
            ArrayList<TaskMatch.TaskMatchStructure> taskCompletenessHits = new ArrayList<>();
            for (FBTask task : tasks.values()) {
                // get task completeness
                taskCompletenessHits.addAll(completeness(task, agent));
            }
            ArrayList<TaskMatch.TaskMatchStructure> hitsMasters = new ArrayList<>();
            //ArrayList<TaskMatch.TaskMatchStructure> hitsComplimentary = new ArrayList<>();

            for (TaskMatch.TaskMatchStructure hit : taskCompletenessHits) {
                if (hit.isMaster()) {
                    hitsMasters.add(hit);
                }/*else{
                  hitsComplimentary.add(hit);
              }*/
            }

            // go submit assembled tasks
            if (!hitsMasters.isEmpty()) {
                //todo find most valuable task - not only most complete
                hitsMasters.sort(Comparator.comparingInt(TaskMatch.TaskMatchStructure::getNameHash));
                hitsMasters.sort(Comparator.comparingInt(TaskMatch.TaskMatchStructure::getReward).reversed());
                hitsMasters.sort(Comparator.comparingDouble(TaskMatch.TaskMatchStructure::getHitRatio).reversed());
                for (TaskMatch.TaskMatchStructure hit : hitsMasters) {
                    //todo: debug
                    HorseRider.challenge(TAG, "getAgentTaskCompleteness: " + agent + " " + hit.getTask() + " " + hit.getHitRatio() + "\n" +
                            "hits:" + hit.getHits());
                    if (hit.getCount() == hit.getTask().getTaskBody().size() &&
                            agent.getAgentInfo().getAcceptedTask() != null &&
                            agent.getAgentInfo().getAcceptedTask().equals(hit.getTask().getName())) { //task is done
                        HorseRider.challenge(TAG, "getAgentTaskCompleteness: " + agent + " " + hit.getTask() + " is DONE ");
                        //if (hit.getTaskPoint().getRotated(hit.getRotation()).equals(hit.getBodyPoint())) {  // task is positioned
                        if (hit.bodyEquivalentOf(Point.zero()).equals((Point.zero()))) {  // task is positioned
                            HorseRider.challenge(TAG, "getAgentTaskCompleteness: " + agent + " " + hit.getTask() + " is positioned !!!!!!!!!!!!!!!!!!");
                            FBGoal submit = new FBGoalGoSubmit(agent, hit.getTask(), hit.getRotation(), step);
                            if (submit.getPlan() != null) {
                                HorseRider.challenge(TAG, "getAgentTaskCompleteness: " + agent + " " + hit.getTask() + " has plan !!!!!!!!!!!!!!!!!!");
                                commitToPlan(agentPlans, agent, hit.getTask(), submit, step);
                                // todo: what if more agent can submit the task and the other is closer?
                                break;
                            }
                        } else {
                            //TODO: reposition to submit
                            HorseRider.challenge(TAG, "getAgentTaskCompleteness: " + agent + " " + hit.getTask() + " is not positioned:\n" +
                                    "body hit:" + hit.bodyEquivalentOf(Point.zero()) + "\n" +
                                    "task hit:" + hit.taskEquivalentOf(Point.zero()));
                            FBGoal submit = new FBGoalGoSubmit(agent, hit.getTask(), hit.getRotation(), step);
                            if (submit.getPlan() != null) {
                                HorseRider.challenge(TAG, "getAgentTaskCompleteness: " + agent + " " + hit.getTask() + " had plan:\n" +
                                        submit.getPlan());

                            }
                        }
                    }
                }
            }
            //add un assembled to queue
            if (!agentPlans.containsKey(agent)) { // does not have plan
                agentTaskPossibilities.addAll(taskCompletenessHits);
            }
        }
        return agentTaskPossibilities;
    }

    private void commitToPlan(HashMap<FBAgent, FBGoal> agentPlans, FBAgent agent, FBTask task, FBGoal plan, int step) {
        agentPlans.put(agent, plan);
        agent.addOrder(plan, step);
        taskWorkedOnIndex.put(task.getName(), 1);
    }

    //TODO: queue all needed agents?
    //todo: take into account distance
    //todo: test - something wrong here!!!
    private ArrayList<PlanOption> connectSupplementAgent(TaskMatch.TaskMatchStructure hitMaster, ArrayList<TaskMatch.TaskMatchStructure> hitsComplimentary, HashMap<FBAgent, FBGoal> agentPlans, int step) {
        ArrayList<PlanOption> options = new ArrayList<>();
        for (TaskMatch.TaskMatchStructure hitComplimentary : hitsComplimentary) {
            if (hitComplimentary.getTask().equals(hitMaster.getTask())) { // same task
                if (hitComplimentary.getAgent() == hitMaster.getAgent()) continue; //skip already used agent
                if (agentPlans.containsKey(hitComplimentary.getAgent())) continue; // agent has plan
                if (agentPlans.containsKey(hitMaster.getAgent())) continue; // agent has plan
                if (taskWorkedOnIndex.containsKey(hitComplimentary.getTask()))
                    continue;  // task is worked on by somebody else
                if (hitMaster.getAgent().getPosition(step).limitedDistance(hitComplimentary.getAgent().getPosition(step)) > FBConstants.MAX_SIM_JOIN_STEP)
                    continue;
                JoinStructure joinStructure = findConnection(hitMaster, hitComplimentary);
                if (joinStructure != null) {
                    FBJoinBodies joinJob = new FBJoinBodies(hitMaster, hitComplimentary, joinStructure);
                    //HorseRider.challenge(TAG, "connectSupplementAgent: get body Join: " + joinJob);
                    HashSet<FBAgent> agents = new HashSet<>();
                    agents.add(hitComplimentary.getAgent());
                    agents.add(hitMaster.getAgent());
                    HashMap<FBAgent, FBGoal> plans = joinJob.makeGoals(agents, snapshotMap, step);
                    //HorseRider.challenge(TAG, "connectSupplementAgent: get body Join: " + plans);
                    if (plans != null && plans.get(hitComplimentary.getAgent()) != null) {                // new plans
                        //HorseRider.challenge(TAG, "connectSupplementAgent: plan " + mostPromisingTask.getAgent().getBody() + " to " + taskHit.getAgent().getBody() + " for " + taskHit.getTask() + " as " + plans);
                        //int planSize = plans.get(mostPromisingTask.getAgent()).getPlan().planSize();
                        PlanOption planOption = new PlanOption();
                        planOption.task = hitMaster.getTask();
                        planOption.agents = agents;
                        planOption.plans = plans;
                        planOption.planSize = plans.get(hitMaster.getAgent()).getPlan().size();
                        planOption.joinedRatio = hitMaster.getHitRatio() + hitComplimentary.getHitRatio();
                        //TODO: add task value and completeness
                        options.add(planOption);
                    }
                }

            }
        }
        return options;
    }

    /**
     * gets non overlapping but connecting and on the edge
     *
     * @param taskBase       starting structure
     * @param taskSupplement connecting structure
     * @return point and direction of connection or null
     */
    //todo: redo and clear ballast
    public JoinStructure findConnection(TaskMatch.TaskMatchStructure taskBase, TaskMatch.TaskMatchStructure taskSupplement) {
        HashSet<Point> sumHitPoints = new HashSet<>();
        sumHitPoints.addAll(taskSupplement.getHits());
        sumHitPoints.addAll(taskBase.getHits());
        if (sumHitPoints.size() == taskSupplement.getCount() + taskBase.getCount()) { // not overlapping
            //lets find neighbouring fields
            for (Point neighbour : PlanHelper.generateDirections()) { // four vectors
                Point hit = null;
                boolean conflict = false;
                for (Point newHitPoint : taskSupplement.getHits()) {
                    if (taskBase.getHits().contains(newHitPoint.diff(neighbour))) {
                        //found it
                        hit = newHitPoint.diff(neighbour);
                        Point neededToBeFreeInSupplement = taskSupplement.bodyEquivalentOf(newHitPoint).diff(neighbour);//.getRotated(taskSupplement.getRotation().mirrored()));
                        Point neededToBeFreeInMaster = taskBase.bodyEquivalentOf(newHitPoint);//.diff(neighbour.getRotated(taskSupplement.getRotation().mirrored()));
                        if (neededToBeFreeInSupplement.equals(Point.zero()) ||
                                taskSupplement.getAgent().getBody().getList().contains(neededToBeFreeInSupplement)) {
                            conflict = true; //joining place not accessible in supplement
                        } else if (neededToBeFreeInMaster.equals(Point.zero()) ||
                                taskBase.getAgent().getBody().getList().contains(neededToBeFreeInMaster)) {
                            conflict = true; //joining place not accessible in master
                            //todo: remove conflict?
                        }
                    }
                }
                if (hit != null && !conflict) {
                    //todo check if something is not interfering elsewhere ? ->skip
                    //todo: find disconnect point an propagate it with connect point
                    HashSet<Point> checked = new HashSet<>();
                    checked.add(Point.zero());
                    PointAndDir disconnect = findConnectionBeforeTask(Point.zero(), taskSupplement, checked);
                    if (disconnect == null) return null;
                    //                       (master point of connection, direction to slave), (slave last point, direction from where to disconnect)
                    return new JoinStructure(hit, neighbour.getDirection(), disconnect.getPoint(), disconnect.getDir());//PointAndDir[]{new PointAndDir(hit, neighbour.getDirection()), disconnect};
                }
            }
        }
        return null;
    }

    private PointAndDir findConnectionBeforeTask(Point source, TaskMatch.TaskMatchStructure taskSupplement, HashSet<Point> checked) {
        PointAndDir disconnect = null;
        for (Point blockPoint : taskSupplement.getAgent().getBody().getLinked(source)) {
            if (checked.contains(blockPoint)) continue;
            checked.add(blockPoint);
            PointAndDir foundDisconnect;
            if (taskSupplement.getHits().contains(taskSupplement.taskEquivalentOf(blockPoint))) { // source is connected to block in task
                foundDisconnect = new PointAndDir(taskSupplement.taskEquivalentOf(blockPoint),
                        taskSupplement.taskEquivalentOf(source).diff(taskSupplement.taskEquivalentOf(blockPoint)).getDirection());
            } else {
                foundDisconnect = findConnectionBeforeTask(blockPoint, taskSupplement, checked);
            }
            if (disconnect != null && foundDisconnect != null) {
                HorseRider.yell(TAG, "findConnectionBeforeTask: " + taskSupplement.getAgent() +
                        " cyclic connection? " + taskSupplement + " " + source + " " + blockPoint +
                        " " + disconnect + " " + taskSupplement.getAgent().getBody());
            } else if (foundDisconnect != null) {
                disconnect = foundDisconnect;
            }

        }
        return disconnect;
    }

    private static class PlanOption {
        double joinedRatio;
        HashMap<FBAgent, FBGoal> plans;
        FBTask task;
        HashSet<FBAgent> agents;
        int planSize;

        double getValue() {
            return joinedRatio * task.getReward();// / planSize; //TODO: re-eval;
        }

        int getNameHash() {
            return ("" + this.task.getName() + agents.stream().map(Agent::getName).collect(Collectors.joining(""))).hashCode();
        }

        int getPlanSize() {
            return planSize;
        }
    }

    private static class TaskInterest {
        FBTask task;
        double value;

        double getValue() {
            return value;
        }

        int getNameHash() {
            return task.getName().hashCode();
        }

        public int getReward() {
            return task.getReward();
        }
    }
}

