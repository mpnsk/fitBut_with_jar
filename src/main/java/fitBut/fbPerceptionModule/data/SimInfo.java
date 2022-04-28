package fitBut.fbPerceptionModule.data;

import fitBut.fbMultiagent.FBRegister;
import fitBut.fbMultiagent.FBTask;
import eis.iilang.Parameter;

import java.util.HashMap;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public class SimInfo {
    private boolean simStarted = false;
    private int steps;
    private boolean requestingAction;
    private int score;
    private long deadline;
    private long timestamp;
    private int step;
    private ActionResult lastActionResult;
    private int actionID;
    private String lastAction;
    private Parameter lastActionParams;
    private String team;
    private final HashMap<String, FBTask> tasksPool = new HashMap<>();
    private final HashMap<String, BlockType> blockIndex = new HashMap<>();
    private int teamSize;
    private int vision;
    private boolean fromBackup = false;

    public void setSimStarted(boolean simStarted) {
        this.simStarted = simStarted;
        fromBackup = true;
    }


    public void setTeam(String team) {
        this.team = team;
    }


    public void setVision(int vision) {
        this.vision = vision;
        FBRegister.GlobalVars.setVision(vision);
    }

    public int getVision() {
        return vision;
    }

    public String getTeam() {
        return team;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }

    public void setRequestingAction(boolean requestingAction) {
        this.requestingAction = requestingAction;
    }

    public boolean getRequestingAction() {
        return requestingAction;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getPerceptStep() {
        return step;
    }

    public void setLastActionResult(ActionResult lastActionResult) {
        this.lastActionResult = lastActionResult;
    }

    public ActionResult getLastActionResult() {
        return lastActionResult;
    }

    public void setActionID(int actionID) {
        this.actionID = actionID;
    }

    public int getActionID() {
        return actionID;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastActionParams(Parameter lastActionParams) {
        this.lastActionParams = lastActionParams; //todo: extract parameter ouf of this scope!
    }

    public Parameter getLastActionParams() {
        return lastActionParams;
    }

    public void addTask(FBTask fbTask) {
        //TODO: add change control?
        if (!tasksPool.containsKey(fbTask.getName())) {
            insertTask(fbTask);
        }
    }

    private void insertTask(FBTask fbTask) {
        tasksPool.put(fbTask.getName(), fbTask);
    }

    public BlockType getBlockType(String blockName) {
        BlockType blockType;
        if (blockIndex.containsKey(blockName)) {
            blockType = blockIndex.get(blockName);
        } else {
            blockType = new BlockType(blockName);
            blockIndex.put(blockName, blockType);
        }
        return blockType;
    }

    public HashMap<String, FBTask> getTaskList() {
        return tasksPool;
    }

    public void dumpTasks() {
        tasksPool.clear();
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public boolean isSimStarted() {
        return simStarted;
    }

    public void setFromBackup(boolean fromBackup) {
        this.fromBackup = fromBackup;
    }

    public boolean isLoadedFromBackup() {
        return fromBackup;
    }
}
