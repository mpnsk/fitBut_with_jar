package fitBut.fbMultiagent;

import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBGroupMap;
import fitBut.fbEnvironment.FBMap;
import fitBut.fbEnvironment.FBMapLayer;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbPerceptionModule.data.BlockType;
import fitBut.fbReasoningModule.fbGoals.FBGoal;
import fitBut.fbReasoningModule.fbGoals.FBGoalHoard;
import fitBut.fbReasoningModule.fbGoals.fbMultiGoals.FBGoalAssembleTasks;
import fitBut.fbReasoningModule.fbGoals.fbMultiGoals.FBGoalExplore;
import fitBut.utils.Point;
import fitBut.utils.exceptions.ShouldNeverHappen;
import fitBut.utils.logging.HorseRider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FBGroup {
    private static final String TAG = "FBGroup";
    private final FBGroupMap groupMap;
    private final FBAgent groupFounder;

    private final ConcurrentHashMap<FBAgent, Point> memberShift;
    private boolean active = true;
    private FBMapPlain reserveMap;
    private int lastStep = -1;
    private FBMapPlain groupMapSnapshot = null;
    private ConcurrentHashMap<String, Integer> taskWorkedOnIndex;
    private final HashMap<Integer, Integer> ootIndex = new HashMap<>();
    private int prevLimitAgentCountBy = 0;

    private String getFounderName() {
        return groupFounder.getName();
    }

    void printGroup() {
        StringBuilder groupInfo = new StringBuilder("Founder: " + groupFounder.getName() + "\nMembers: ");

        for (FBAgent agent : memberShift.keySet()) {
            groupInfo.append(agent.getName())
                    .append(": ").append(memberShift.get(agent)).append(", ");
        }
        HorseRider.inquire(TAG, "printGroup: " + groupInfo);
    }

    public FBGroup(FBAgent agent) {
        groupFounder = agent;
        memberShift = new ConcurrentHashMap<>();
        groupMap = new FBGroupMap(this);
        memberShift.put(agent, new Point(0, 0));
    }

    void setInactive() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    int getGroupSize() {
        return memberShift.size();
    }

    void registerNewMember(FBAgent fromAgent, FBAgent newAgent, Point vector) {
        Point shiftVector = new Point(vector);
        shiftVector.translate(-newAgent.getLatestLocalPosition().x, -newAgent.getLatestLocalPosition().y); //remove agent difference
        shiftVector.translate(fromAgent.getLatestLocalPosition().x, fromAgent.getLatestLocalPosition().y); //add new origin

        newAgent.setGroup(this);
        memberShift.put(newAgent, shiftVector);
    }

    void importGroup(FBAgent fromAgent, FBAgent otherGroupAgent, Point vector) {
        Point shiftVector = new Point(vector);
        shiftVector.translate(-otherGroupAgent.getLatestLocalPosition().x, -otherGroupAgent.getLatestLocalPosition().y); //remove agent difference
        shiftVector.translate(-otherGroupAgent.getGroupMemberShift().x, -otherGroupAgent.getGroupMemberShift().y);
        shiftVector.translate(fromAgent.getGroupMemberShift().x, fromAgent.getGroupMemberShift().y); //add new origin
        shiftVector.translate(fromAgent.getLatestLocalPosition().x, fromAgent.getLatestLocalPosition().y); //add new origin

        FBGroup oldGroup = otherGroupAgent.getGroup();
        ConcurrentHashMap<FBAgent, Point> otherGroup = oldGroup.getMembers();
        for (FBAgent transientMember : otherGroup.keySet()) {
            Point point = otherGroup.get(transientMember);
            Point newGroupMemberShift = new Point(shiftVector);
            newGroupMemberShift.translate(point.x, point.y);
            memberShift.put(transientMember, newGroupMemberShift);
            transientMember.setGroup(this);
        }
        oldGroup.setInactive();
    }

    public ConcurrentHashMap<FBAgent, Point> getMembers() {
        return memberShift;
    }

    public Point getShift(FBAgent fbAgent) {
        return memberShift.getOrDefault(fbAgent, new Point(0, 0));
    }

    /**
     * translation from perception to perception
     *
     * @param fromAgent agent seeing
     * @param toAgent   agent receiving
     * @param point     position relative to origin agent
     * @return point translated relative to receiving agent
     */
    private static Point translateEyeToEye(FBAgent fromAgent, FBAgent toAgent, Point point) {
        return translate(fromAgent, toAgent, point.sub(fromAgent.getLatestLocalPosition()).add(toAgent.getLatestLocalPosition()));
    }

    /**
     * non-modifying variant of translateEyeToEye
     *
     * @param fromAgent agent seeing
     * @param toAgent   agent receiving
     * @param point     position relative to origin agent
     * @return new point relative to receiving agent
     */
    public static Point getTranslatedEyeToEye(FBAgent fromAgent, FBAgent toAgent, Point point) {
        return translateEyeToEye(fromAgent, toAgent, new Point(point));
    }

    /**
     * translation from map to map
     *
     * @param fromAgent origin agent
     * @param toAgent   receiving agent
     * @param point     position in origin agent coordination system
     * @return point receiving agent point
     */
    public static Point translate(FBAgent fromAgent, FBAgent toAgent, Point point) {
        if (fromAgent.getGroup() != toAgent.getGroup()) {
            HorseRider.yell(TAG, "translate: Trying to shift between two different groups! " +
                    fromAgent.getName() + " (group of " + fromAgent.getGroup().groupFounder + ") " +
                    toAgent.getName() + " (group of " + toAgent.getGroup().groupFounder + ") ");
        }
        return point.add(fromAgent.getGroupMemberShift()).sub(toAgent.getGroupMemberShift());
    }

    public void printMap() {
        groupMap.printMap();
    }

    public FBMap getGroupMapSnapshot() {
        if (groupMapSnapshot == null) {
            printGroup();
            groupMap.printMap();
            throw new ShouldNeverHappen(TAG + " getGroupMapSnapshot: null " + getName() + "");
            // HorseRider.yell(TAG, "getGroupMapSnapshot: null " + getName() + " generating new snapshot");
        }
        return groupMapSnapshot;
    }

    public String getName() {
        StringBuilder names = new StringBuilder();
        for (FBAgent agent : getMembers().keySet()) {
            names.append(agent.getName()).append(" ");
        }
        return "Group of " + getFounderName() + ": " + names;
    }

    public void runDecisions(int step) {
        this.groupMapSnapshot = this.groupMap.getMapSnapshot(0);
        groupMapSnapshot.printMap();
        Set<FBAgent> undecided = new TreeSet<>();
        getMembers().keySet().forEach(fbAgent -> {
            fbAgent.setGroupSynced(step);
            if (!fbAgent.isBusy(step) && !FBRegister.GlobalVars.isBlackListed(fbAgent)) {
                undecided.add(fbAgent);
            } else {
                fbAgent.informNoMoreGroupDecisions(step);
            }
        });
        int prevStepOOTs = 0;
        int prevPrevStepOOTs = 0;
        if (ootIndex.containsKey(step - 1)) {
            prevStepOOTs = ootIndex.get(step - 1);
        }
        if (ootIndex.containsKey(step - 2)) {
            prevPrevStepOOTs = ootIndex.get(step - 2);
        }

        int ootChange = prevStepOOTs - prevPrevStepOOTs;

        int limitAgentCountBy;
        if (ootChange > 0) {
            limitAgentCountBy = prevLimitAgentCountBy + (ootChange) / 3 * 2; // situation is getting worse
        } else {
            if (ootChange == 0 && prevStepOOTs == 0) {
                limitAgentCountBy = prevLimitAgentCountBy / 3 * 2;           // no oots so slowly let them go
            } else {
                limitAgentCountBy = prevLimitAgentCountBy + (ootChange) / 3; //lets carefully get better
            }
        }
        prevLimitAgentCountBy = limitAgentCountBy;

        if (limitAgentCountBy > 0) {
            HorseRider.warn(TAG, "runDecisions: oot bypass - removing " + limitAgentCountBy + "agents");
            Iterator<FBAgent> iterator = undecided.iterator();
            for (int i = 0; i < limitAgentCountBy; i++) {
                if (iterator.hasNext()) {
                    FBAgent agent = iterator.next();
                    HorseRider.warn(TAG, "runDecisions: removing agent" + agent.getName() + " for OOT complexity");
                    iterator.remove();
                    agent.informNoMoreGroupDecisions(step);
                }
            }
        }

        // lets assemble tasks
        HorseRider.inquire(TAG, "runDecisions: go assemble task" + getName());
        HashMap<FBAgent, FBGoal> plans = new FBGoalAssembleTasks(this).makeGoals(undecided, groupMapSnapshot, step);
        taskAndCheckAgents(step, undecided, plans);

        //hoard
        Iterator<FBAgent> iterator = undecided.iterator();
        while (iterator.hasNext()) {
            FBAgent agent = iterator.next();
            if (checkAgent(step, iterator, agent)) {
                FBGoal goalHoard = new FBGoalHoard();
                if (goalHoard.makePlan(agent) != null) {
                    taskAgent(step, iterator, agent, goalHoard);
                }
            }
        }

        // explore unknown terrain or oldest
        int oldestToBeUsed = Math.max(0, getStep() - 100);
        while (undecided.size() > 0) {
            if (oldestToBeUsed > step) {
                HorseRider.warn(TAG, "runDecisions: run out of steps to trim! " + undecided + " without decision.");
                break;
            }
            HorseRider.challenge(TAG, "runDecisions: get explore plans for: (" + undecided.size() + ") " + undecided + " map trim: " + oldestToBeUsed);

            FBMapPlain snapshotMap;
            if (oldestToBeUsed > 0) {
                snapshotMap = groupMapSnapshot.getMapSnapshot(oldestToBeUsed);
            } else {
                snapshotMap = groupMapSnapshot;
            }
            //snapshotMap.printMap();
            //oldestToBeUsed = Math.max(oldestToBeUsed, getStep()/2);
            //oldestToBeUsed += getStep()/2;

            taskAndCheckAgents(step, undecided, new FBGoalExplore().makeGoals(undecided, snapshotMap, step));
            break; //todo:temp loop bypass
        }

        //for (FBAgent agent : getMembers().keySet()) {
        for (FBAgent agent : undecided) {
            agent.informNoMoreGroupDecisions(step);
        }
    }

    private void taskAndCheckAgents(int step, Set<FBAgent> undecided, HashMap<FBAgent, FBGoal> plans) {
        Iterator<FBAgent> iterator = undecided.iterator();
        while (iterator.hasNext()) {
            FBAgent agent = iterator.next();
            FBGoal goalPlan = plans.get(agent);
            HorseRider.challenge(TAG, "runDecisions: taskAndCheckAgents " + agent + " goalPlan: " + goalPlan);
            if (checkAgent(step, iterator, agent)) {
                taskAgent(step, iterator, agent, goalPlan);
            }
        }
    }

    private void taskAgent(int step, Iterator<FBAgent> iterator, FBAgent agent, FBGoal goalPlan) {
        if (goalPlan != null) {
            HorseRider.challenge(TAG, "taskAgent: task : " + agent + " " + goalPlan);
            agent.addOrder(goalPlan, step); // set order for planned agents
            iterator.remove();
            agent.informNoMoreGroupDecisions(step);
        }
    }

    /**
     * checks if agent is still in decision making
     *
     * @param step     current step
     * @param iterator agent iterator
     * @param agent    agent
     * @return true if agent is still waiting for decisions
     */
    private boolean checkAgent(int step, Iterator<FBAgent> iterator, FBAgent agent) {
        if (!agent.waitingForOrders(step) || agent.isStepTimedOut(step)) { // agent has timed out
            HorseRider.inquire(TAG, "checkAgent: agent: " + agent.getName() + " removed from decision queue");
            iterator.remove();
            return false;
        }
        return true;
    }

    private void newStep() {
        reserveMap = new FBMapPlain(getName() + "reserved");
        taskWorkedOnIndex = new ConcurrentHashMap<>();
    }

    private boolean checkStep(int step) {
        if (step < lastStep) {
            return false;
        } else if (step > lastStep) {
            lastStep = step;
            newStep();
        }
        return true;
    }

    public synchronized boolean reserveFuture(FBMapLayer future, int step) {
        if (!checkStep(step) || future == null) {
            HorseRider.yell(TAG, "reserveStructure: " + future + " error or reporting with info from previous step: " + step + " vs " + lastStep);
            return false;
        }

        if (FBMap.mergeAble(reserveMap, future)) {
            reserveMap.importLayer(future);
            //HorseRider.challenge(TAG, "reserveFuture: merge able " + reserveMap.getName() + " and " + future.getName());
            //reserveMap.printMap();
            //future.printMap();
            return true;
        } else {
            HorseRider.inquire(TAG, "reserveFuture: can't merge \n" + future + "\nin to\n" + reserveMap);
        }
        //reserveMap.printMap();
        return false;
    }

    public boolean outOfSync(int step) {
        if (!checkStep(step)) {
            HorseRider.yell(TAG, "outOfSync: reporting with info from previous step: " + step + " vs " + lastStep);
            return true;
        }
        return false;
    }

    public ConcurrentHashMap<String, Integer> getTaskWorkedOnIndex() {
        //checkStep(step);
        if (taskWorkedOnIndex == null) {
            newStep();
        }
        return this.taskWorkedOnIndex;
    }

    public void addTaskWorkedOnIndex(int step, String taskName, int agentNum) {
        checkStep(step);
        this.taskWorkedOnIndex.put(taskName, agentNum);
    }

    public int getStep() {
        return lastStep;
    }

    /*public boolean isPossibleBorder(Point position, Direction plannedDirection) {

        FBMap.Border b = new FBMap.Border();
        b.addBorder(position, plannedDirection);
        for (FBAgent member : memberShift.keySet()) {
            if (b.isOutside(member.getPosition())) return false;
        }
        return true;
    }*/

    //todo: Implement
    @Deprecated
    public boolean isBlockInteresting(@SuppressWarnings("unused") BlockType blockType) {
        return true;
    }

    public FBAgent getGroupFounder() {
        return groupFounder;
    }

    public synchronized void addOOTAgent(int step) {
        this.ootIndex.putIfAbsent(step, 0);
        this.ootIndex.put(step, ootIndex.get(step) + 1);
    }
}

