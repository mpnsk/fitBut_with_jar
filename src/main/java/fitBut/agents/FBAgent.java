package fitBut.agents;


import fitBut.MailService;
import fitBut.agents.utils.FBAgentStepValues;
import fitBut.fbActions.*;
import fitBut.fbEnvironment.FBBody;
import fitBut.fbEnvironment.FBMap;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbMultiagent.FBGroup;
import fitBut.fbMultiagent.FBRegister;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbPerceptionModule.FBPerceptsProcessorV2;
import fitBut.fbPerceptionModule.data.ActionResult;
import fitBut.fbPerceptionModule.data.AgentInfo;
import fitBut.fbPerceptionModule.data.SimInfo;
import fitBut.fbReasoningModule.fbGoals.*;
import fitBut.fbReasoningModule.fbGoals.utils.PrioritySelector;
import fitBut.utils.Point;
import fitBut.utils.StepAction;
import fitBut.utils.exceptions.ShouldNeverHappen;
import fitBut.utils.logging.HorseRider;
import eis.iilang.Percept;

import java.util.*;

import static fitBut.utils.FBConstants.*;

public class FBAgent extends Agent implements Comparable<FBAgent> {
    private static final String TAG = "FBAgent";
    private final FBPerceptsProcessorV2 perceptsProcessor = new FBPerceptsProcessorV2();
    private final AgentInfo agentInfo = new AgentInfo();
    private final SimInfo simInfo = new SimInfo();


    private FBBody agentBody = new FBBody(this); //TODO: move to stepValues

    public HashMap<Integer, FBAgentStepValues> stepValues = new HashMap<>();


    private FBGroup group;
    FBMap map;
    private int latestStep = -1;


    /**
     * test agent boot
     *
     * @param name test name
     */
    public FBAgent(String name) {
        super(name, new MailService());
        HorseRider.yell(TAG, "FBAgent: !!!!!!!!!! TESTING BOOT !!!!!!!!!!!");
        agentInfo.setName(name);
        simInfo.setVision(5);
        map = new FBMapPlain(name);
        group = new FBGroup(this);
    }

    /**
     * constructor for SimAgent
     */
    public FBAgent() {
        if (!(this instanceof FBSimAgent)) {
            throw new ShouldNeverHappen(TAG + " FBAgent: " + this.getName() + " this constructor is reserved for virtual agents");
        }
    }

    public FBGroup getGroup() {
        return (group);
    }

    public FBMap getLocalMap() {
        return (map);
    }

    public FBMap getMap() {
        if (this.getGroup().isActive()) {
            try {
                return getGroup().getGroupMapSnapshot();
            } catch (ShouldNeverHappen h) {
                HorseRider.yell(TAG, "getMap: " + getName() + " didn't get group map ", h);
                return getLocalMap();
            }
        } else {
            throw new ShouldNeverHappen(TAG + " getMap: " + getName() + " ");
        }

    }

    /**
     * runs check on seen entities in local map
     *
     * @return list of position
     */
    public HashSet<Point> getSeeingFriendlies(int step) {
        return map.getSeeingAgents(getLocalPosition(step), simInfo.getVision());
    }

    /**
     * agent position translation
     *
     * @param vector movement
     * @param step
     */
    public void updateMapPosition(Point vector, int step) {
        stepValues.get(step).getAgentMapPos().translate(vector);
        HorseRider.inquire(TAG, "updateMapPosition: " + getName() + " by " + vector);
    }

    /**
     * current position af agent on its map
     *
     * @param step step
     * @return position
     */
    public Point getLocalPosition(int step) {
        return stepValues.get(step).getAgentMapPos();
    }

    public Point getLatestLocalPosition() {
        return stepValues.get(latestStep).getAgentMapPos();
    }

    public void setMapPosition(Point point, int step) {
        stepValues.get(step).setAgentMapPos(point);
    }

    public void setGroup(FBGroup fbGroup) {
        this.group = fbGroup;
    }

    /*public int getStep() {
        return simInfo.getStep();
    }*/

    public Point getGroupMemberShift() {
        return this.getGroup().getShift(this);
    }

    public Point getLatestPosition() {
        return getPosition(latestStep);
    }

    public Point getPosition(int step) {
        Point point = new Point(getLocalPosition(step));
        if (this.getGroup().isActive()) {
            point.translate(getGroupMemberShift().x, getGroupMemberShift().y);
        } else {
            throw new ShouldNeverHappen(TAG + " getPosition: " + getName() + " ");
        }
        return point.getLimited();
    }

    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    @Override
    public void handleMessage(Percept message, String sender) {
    }

    /**
     * @param step in question
     * @return true if agent is already in higher step
     * or if action was set (probably by priority Action)
     */
    @Override
    public boolean isStepTimedOut(int step) {
        return getLatestStep() != step || stepValues.get(step).getStepAction() != null;
    }

    @Override
    public synchronized StepAction step() {
        //set up new step
        //if (!clean) {
        //    HorseRider.yell(TAG, "step: " + getName() + " agent turned on without cleanup!");
        agentInfo.setAcceptedTask(null);

        //}
        getLocalMap().checkLimitChange();
        if (stepValues.containsKey(latestStep)) {
            getLocalPosition(latestStep).limit();
        }

        if (evaluatePercepts()) return null; //if percepts are incomplete quit

        boolean skippedStep = false;
        int step = simInfo.getPerceptStep();

        if (stepValues.containsKey(step)) {
            HorseRider.yell(TAG, "step: " + getName() + " step values already initialized!!! " + step);
        }
        if (agentInfo.getName().equals("Unknown")) agentInfo.setName(getName());

        stepValues.put(step, new FBAgentStepValues(stepValues.getOrDefault(latestStep, null)));
        FBGoal lastAction = null;
        if (stepValues.containsKey(latestStep)) {
            lastAction = stepValues.get(latestStep).getFinalAction();
        }

        boolean hotWireBoot = false; //indicates boot in higher step -> need to check attached blocks
        if (step != latestStep + 1) {
            skippedStep = true;
            if (latestStep == -1 && step > 5) hotWireBoot = true;
        }
        int previousStep = latestStep;
        latestStep = step;

        //evaluate last action (like step)
        evaluateLatsStepResult(lastAction, skippedStep, step);

        HorseRider.inform(TAG, "action:  +++++++++ in step " + step +
                " agent " + agentInfo.getName() + " is at " + getLocalPosition(step) +" ++++++++++");

        // update map based on perceptions
        perceptsProcessor.runMapUpdate(map, this, step);

        //second action eval (with new map data)
        if (lastAction != null) {
            if (lastAction.getAction().getClass().toString().equals(FBAction.getActionClassFromString(simInfo.getLastAction()))) {
                lastAction.getAction().getAgentActionFeedbackReeval(simInfo.getLastActionResult(), this, step);
            }
        }

        //check agent body
        if (hotWireBoot) {
            //HorseRider.inquire(TAG, "step: "+getName()+" hotwiring!");
            FBGoal goal = agentBody.checkSecretlyConnectedBlocks(map, getLocalPosition(step));
            if(goal!=null){
                addOrder(goal,step);
            }
        }
        agentBody.checkIntegrity(map, getLocalPosition(step));
        HorseRider.inquire(TAG, "step: "+ getBody());


        //report finished first stage
        new Thread(() -> mailbox.reportIn(this, step)).start();

        //local decisions
        new Thread(() -> evalLocalDecisions(step)).start();

        //pause until timeout or decision
        HorseRider.challenge(TAG, "action: " + getName() + " wait for timeout");
        //long timeLimit = simInfo.getDeadline() - TIME_RESERVE;

        int timeLimitDiff;
        if (stepValues.get(previousStep) != null) {
            timeLimitDiff = stepValues.get(previousStep).getDecisionTimeReserve();
        } else {
            timeLimitDiff = TIME_RESERVE;
        }
        long decisionTimeLimit = simInfo.getDeadline() - timeLimitDiff;
        while (this.stepValues.get(step).getStepAction() == null) {
            long timeRemainder = (decisionTimeLimit - System.currentTimeMillis());
            if (timeRemainder < DO_NOT_SLEEP_TIME) break;//do not sleep for less than DO_NOT_SLEEP_TIME
            try {
                HorseRider.challenge(TAG, "action: " + getName() +
                        " go to sleep for more orders; at " + System.currentTimeMillis() + " for " + (decisionTimeLimit - System.currentTimeMillis()) + " to " + decisionTimeLimit);
                wait(timeRemainder / 2);
            } catch (InterruptedException | IllegalArgumentException e) {
                //e.printStackTrace();
                HorseRider.warn(TAG, "action: " + getName() + " has been sleep interrupted ", e);
            }
        }
        //FBRegister.GlobalVars.setDecisionTimeReserve(step, getName(), Math.toIntExact(decisionTimeLimit - System.currentTimeMillis()));

        //time for new orders is out
        HorseRider.challenge(TAG, "action: " + getName() + " after timeout (" + timeLimitDiff + ") or decision: " + this.stepValues.get(step).getStepAction());
        this.stepValues.get(step).setNotInDecisionMaking();

        //if still no order get backup
        if (this.stepValues.get(step).getStepAction() == null) {
            new Thread(() -> timeOutBackupOrder(step)).start();
        }
        long submissionTimeLimit = simInfo.getDeadline() - MINIMAL_TIME_RESERVE;
        while (this.stepValues.get(step).getStepAction() == null) {
            long timeRemainder = (submissionTimeLimit - System.currentTimeMillis());
            if (timeRemainder < DO_NOT_SLEEP_TIME) break;//do not sleep for less than DO_NOT_SLEEP_TIME
            try {
                HorseRider.challenge(TAG, "action: " + getName() +
                        " go to sleep for reservation; at " + System.currentTimeMillis() + " for " + (submissionTimeLimit - System.currentTimeMillis()) + " to " + submissionTimeLimit);
                wait(timeRemainder / 2);
            } catch (InterruptedException | IllegalArgumentException e) {
                //e.printStackTrace();
                HorseRider.warn(TAG, "action: " + getName() + " has been sleep interrupted ", e);
            }
        }
        //FBRegister.GlobalVars.setReservationTimeReserve(step, getName(), Math.toIntExact(submissionTimeLimit - System.currentTimeMillis()));
        stepValues.get(step).setDecisionTimeReserve(Math.toIntExact(submissionTimeLimit - System.currentTimeMillis()), timeLimitDiff);


        List<Percept> unusedPercepts = clearPercepts();
        if (!unusedPercepts.isEmpty()) {
            HorseRider.yell(TAG, "step: " + name + "there were unused percepts!!!");
            for (Percept percept : unusedPercepts) {

                HorseRider.yell(TAG, "step: " + name + " unused percept: " + percept.toProlog());
            }
        }

        if (simInfo.getDeadline() - System.currentTimeMillis() - MINIMAL_TIME_RESERVE < 0) {
            HorseRider.warn(TAG, "step: " + step + " " + getName() +
                    " missed deadline for returning action " + (simInfo.getDeadline() - System.currentTimeMillis()) + " (" + (simInfo.getDeadline()) + "/" + System.currentTimeMillis() + ")");
            this.stepValues.get(step).setStepAction(null);
        } else {
            if (this.stepValues.get(step).getStepAction() != null) {
                HorseRider.inform(TAG, "step: " + step + " " + getName() +
                        " returning action " + (simInfo.getDeadline() - System.currentTimeMillis()) + " " + this.stepValues.get(step).getStepAction().getAction().getEisAction().toProlog() + " \tfrom goal " + this.stepValues.get(step).getStepAction());
            } else {
                HorseRider.inform(TAG, "step: " + step + " " + getName() +
                        " returning action " + (simInfo.getDeadline() - System.currentTimeMillis()) + " null ");
            }
        }

        this.stepValues.get(step).setFinalAction();

        //stepCleanUp(step);
        FBRegister.agentDone(this, step);

        if (this.stepValues.get(step).getFinalAction() != null) {
            return new StepAction(step, this.stepValues.get(step).getFinalAction());
        }
        return new StepAction(step, null);
    }

    private void timeOutBackupOrder(int step) {
        setOrderFromBackup(step);
        HorseRider.warn(TAG, "action: " + getName() +
                " running out of time! Defaulting to action: " + this.stepValues.get(step).getStepAction());// +
        // "\ntime: " + System.currentTimeMillis() + "\nutil: " + simInfo.getDeadline());
        group.addOOTAgent(step);
    }

    private void evalLocalDecisions(int step) {
        if (agentInfo.getDisabled()) {
            // agent is disabled and won't do anything this step
            HorseRider.inquire(TAG, "action: " + agentInfo.getName() + " is disabled at " + getLocalPosition(step));
            addOrder(new FBGoalDoNothing(), step);
            getBody().dropAll();
            setBusy(step);
        } else {
            // run local decision
            runLocalDecision(step);
        }
        informNoMoreAgentDecisions(step);
    }

    private void evaluateLatsStepResult(FBGoal lastAction, boolean skippedStep, int step) {
        if (lastAction != null) {
            if (lastAction.getAction().getClass().toString().equals(FBAction.getActionClassFromString(simInfo.getLastAction()))) {
                lastAction.getAction().getAgentActionFeedback(simInfo.getLastActionResult(), this, step);
            } else {
                HorseRider.yell(TAG, "step: " + getName() + " last action mismatch:" +
                        " agent: " + lastAction.getAction().getClass() +
                        " sim: " + FBAction.getActionClassFromString(simInfo.getLastAction()) + "(" + simInfo.getLastAction() + ")");
                if (lastAction.getAction() instanceof FBMove && skippedStep) { //if action was move check if moved or not
                    if (tryToDisConfirmPosition(step)) {
                        HorseRider.warn(TAG, "step: " + getName() + " guessing that agent moved");
                        lastAction.getAction().getAgentActionFeedback(ActionResult.SUCCESS, this, step);
                    } else {
                        HorseRider.warn(TAG, "step: " + getName() + " guessing that agent failed to move lets test it");
                        lastAction.getAction().getAgentActionFeedback(ActionResult.SUCCESS, this, step);
                        if (tryToDisConfirmPosition(step)) {
                            HorseRider.warn(TAG, "step: " + getName() + " yes failed to move, lets get him back");
                            lastAction.getAction().getAgentActionFeedback(ActionResult.TMP_OP_REVERSE, this, step);
                        } else {
                            HorseRider.warn(TAG, "step: " + getName() + "LOST get him blaclisted!");
                            FBRegister.GlobalVars.blackList(this);
                        }
                    }
                }
            }
        } else if (step > 0) {
            HorseRider.warn(TAG, "step: " + getName() + " last action is null");
        }
    }

    private boolean evaluatePercepts() {
        //get percepts
        List<Percept> percepts = popPercepts();
        HorseRider.inform(TAG, "----------------- step: " + getName() + " with percepts: " + percepts.size());

        //read percepts
        if (percepts.size() == 0) {
            HorseRider.yell(TAG, "step: " + getName() + " does want it's percepts!");
            return true;
        }
        perceptsProcessor.processPercepts(percepts, getName());
        if (!simInfo.isSimStarted() && !simInfo.isLoadedFromBackup()) {
            FBRegister.GlobalVars.getBackupConfig(simInfo);
            HorseRider.warn(TAG, "step: " + getName() + " sim not started loading backup params");
        }
        if (!simInfo.getRequestingAction() || perceptsProcessor.thingsCount() == 0) { //nothing to do

            if (!simInfo.getRequestingAction()) {
                HorseRider.warn(TAG, "step: " + getName() + " action not requested. Quiting.");
            }
            if (perceptsProcessor.thingsCount() == 0) {
                HorseRider.warn(TAG, "step: " + getName() + " no damn vision percepts. Quiting.");
            }
            addPercepts(percepts); // return percepts for future cycle
            return true;
        }
        return false;
    }

    private boolean tryToDisConfirmPosition(int step) {
        return perceptsProcessor.runMapMovedCheckDisconfirmPosition(map, this, step);
    }

    /**
     * local decisions set
     *
     * @param step
     */
    private void runLocalDecision(int step) {
        //dodge clear actions
        FBGoal goalDodge = new FBGoalDodge(step);
        if (goalDodge.makePlan(this) != null) {
            addOrder(goalDodge, step);
        }

        if (stepValues.get(step - 1) != null && stepValues.get(step - 1).getFinalAction() instanceof FBGoalHamperEnemy) {
            FBGoal goalHamper = new FBGoalHamperEnemy((FBGoalHamperEnemy) stepValues.get(step - 1).getFinalAction(), this);
            if (goalHamper.getPlan() != null) {
                addOrder(goalHamper, step);
            }
        }
        //hamper enemy agents
        FBGoal goalHamper = new FBGoalHamperEnemy();
        if (goalHamper.makePlan(this) != null) {
            addOrder(goalHamper, step);
        }

        //go near submit todo:go to highlight
        FBGoalGoNearSubmit goalFBGoalGoNearSubmit = new FBGoalGoNearSubmit();
        if (goalFBGoalGoNearSubmit.makePlan(this) != null) {
            addOrder(goalFBGoalGoNearSubmit, step);
        }
        FBGoalGoGetTask goalFBGoalGoGetTask = new FBGoalGoGetTask();
        if (goalFBGoalGoGetTask.makePlan(this) != null) {
            addOrder(goalFBGoalGoGetTask, step);
        } else if (!((FBMapPlain) map).doesNotHaveTakBoard()) {
            goalFBGoalGoGetTask.makePlan(this);
        }

        //clear obstacles
        FBGoal goalDig = new FBGoalDig();
        if (goalDig.makePlan(this) != null) {
            addOrder(goalDig, step);
        }

    }

    private FBGoal getActionFromPool(int step) {
        if (!stepValues.get(step).getGoalPool().isEmpty()) {
            return stepValues.get(step).getGoalPool().pollLastEntry().getValue();
        } else {
            return new FBGoalDoNothing();
        }
    }

    public FBAgent(String name, MailService service) {
        super(name, service);
        map = new FBMapPlain(name);
        group = new FBGroup(this);

        perceptsProcessor.setAgentInfo(agentInfo);
        perceptsProcessor.setSimInfo(simInfo);
    }

    /**
     * tries to set goal based on reservation system approval
     *
     * @param goal current wanted goal
     * @return true if order was set or no order will be needed
     */
    private synchronized boolean setOrder(FBGoal goal, int step) {
        if (!group.outOfSync(step) && !isStepTimedOut(step)) {
            if (this.stepValues.get(step).getMultiAgentConnectionFlag() && (
                    goal.getAction() instanceof FBMove ||
                            goal.getAction() instanceof FBRotate)) {
                HorseRider.warn(TAG, "setOrder: " + getName() + " trying to move while connected to other agents! " + goal);
                return false;
            }
            if (FBRegister.GlobalVars.isBlackListed(this) || !this.stepValues.get(step).isGroupSynced()) { //group problem .... +- no need reserving
                this.stepValues.get(step).setStepAction(goal);
                return true;
            }
            try {
                if (!group.reserveFuture(goal.getFuture(this), step)) {
                    HorseRider.warn(TAG, "reserveRotate: " + this + " failed to reserve structure for order " + goal);
                    FBAction action = goal.getAction();
                    if (action instanceof FBConnect ||
                            action instanceof FBDetach ||
                            action instanceof FBDisconnect ||
                            action instanceof FBSkip ||
                            action instanceof FBSubmit) { //action which can be tried without reservation //todo: add notice to actions in conflict
                        HorseRider.inquire(TAG, "setOrder: " + getName() + " is submitting conflict order" + goal);
                        this.stepValues.get(step).setStepAction(goal);
                        return true;
                    }
                    return false;
                }
            } catch (Exception e) {
                HorseRider.yell(TAG, "setOrder: exception in reservation system of " + getName(), e);
                return false;
            }
            HorseRider.inquire(TAG, "setOrder: " + getName() + " has order " + goal);
            this.stepValues.get(step).setStepAction(goal);
        } else {
            HorseRider.yell(TAG, "setOrder: agent " + this + " out of sync " + step);
        }
        return true;
    }

    public FBBody getBody() {
        return this.agentBody;
    }

    @Override
    public String toString() {
        return getName() + " " + getLocalPosition(latestStep) + "/" + getPosition(latestStep);
    }

    /**
     * adds order to the queue based on priority
     *
     * @param goal order
     * @param step
     */
    public void addOrder(FBGoal goal, int step) {
        if (step == getLatestStep() && stepValues.get(step).isInDecisionMaking()) {
            addPriorityOrder(goal, PrioritySelector.getBasePriority(goal), step);
        } else {
            HorseRider.warn(TAG, "addOrder: " + getName() + " Ignoring order (step " + step + "/" + step + ") " + goal);
        }
    }

    /**
     * adds order with specific priority
     *
     * @param goal     order
     * @param priority specific int
     */
    private void addPriorityOrder(FBGoal goal, int priority, int step) {
        stepValues.get(step).getGoalPool().put(priority, goal); //todo: change from tree to priority queue
        HorseRider.inquire(TAG, "addOrder: " + getName() + " has backup order " + goal +
                " with priority: " + priority);
    }

    public void informNoMoreGroupDecisions(int step) {
        if (step == getLatestStep() && stepValues.get(step).isInDecisionMaking()) {
            stepValues.get(step).setGroupDecisionsDone();
            HorseRider.challenge(TAG, "informNoMoreGroupDecisions: " + getName() + " group done");
            selectDecision(step);
        }
    }

    private void informNoMoreAgentDecisions(int step) {
        stepValues.get(step).setAgentDecisionsDone();
        HorseRider.challenge(TAG, "informNoMoreAgentDecisions: " + getName() + " local done");
        selectDecision(step);
    }

    private void selectDecision(int step) {
        if (this.stepValues.get(step).getStepAction() == null && this.stepValues.get(step).isAgentDecisionsDone() && this.stepValues.get(step).isGroupDecisionsDone()) {
            this.stepValues.get(step).setNotInDecisionMaking();
            setOrderFromBackup(step);
        }
    }

    private synchronized void setOrderFromBackup(int step) {
        boolean needOrder = true;
        while (needOrder) {
            needOrder = !setOrder(getActionFromPool(step), step);
        }
        notify();
    }

    public HashMap<String, FBTask> getTasks() {
        return simInfo.getTaskList();
    }

    public void setMultipleAgentsOnBlock(int step) {
        if (isStepTimedOut(step)) {
            HorseRider.warn(TAG, "setMultipleAgentsOnBlock: info out of step: " + step + " in " + getLatestStep());
        } else {
            this.stepValues.get(step).setMultiAgentConnectionFlag(true);
        }
    }

    public boolean hasMultipleAgentsOnBlock(int step) {
        return this.stepValues.get(step).getMultiAgentConnectionFlag();
    }

    public void setGroupSynced(int step) {
        HorseRider.inquire(TAG, "setGroupSynced: " + getName() + " in " + getLatestStep() + " from " + step);
        if (step == getLatestStep()) {
            this.stepValues.get(step).setGroupSynced(true);
        }
    }

    public void setBusy(int step) {
        this.stepValues.get(step).setBusy();
    }

    public boolean isBusy(int step) {
        return this.stepValues.get(step).getBusy();
    }

    public void setBody(FBBody newBody) {
        this.agentBody = newBody;
    }

    public SimInfo getSimInfo() {
        return simInfo;
    }

    public boolean waitingForOrders(int step) {
        return this.stepValues.get(step).isInDecisionMaking();
    }


    @Override
    public int compareTo(FBAgent o) {
        return this.getName().compareTo(o.getName());
    }

    public int getLatestStep() {
        return latestStep;
    }
}
