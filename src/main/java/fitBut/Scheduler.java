package fitBut;

import fitBut.agents.FBAgent;
import fitBut.fbActions.FBSubmit;
import fitBut.fbReasoningModule.fbGoals.*;
import fitBut.utils.StepAction;
import fitBut.utils.exceptions.NotImplementedException;
import fitBut.utils.logging.HorseRider;
import eis.AgentListener;
import eis.EnvironmentListener;
import eis.exceptions.ActException;
import eis.exceptions.AgentException;
import eis.exceptions.PerceiveException;
import eis.exceptions.RelationException;
import eis.iilang.EnvironmentState;
import eis.iilang.Percept;
import fitBut.agents.Agent;
import massim.eismassim.EnvironmentInterface;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static fitBut.utils.logging.HorseRider.makeLogFor;

/**
 * A scheduler for agent creation and execution.
 * EISMASSim scheduling needs to be enabled (via config), so that getAllPercepts()
 * blocks until new percepts are available!
 * (Also, queued and notifications should be disabled)
 */
public class Scheduler implements AgentListener, EnvironmentListener {

    private static final String TAG = "Scheduler";
    private int score = 0;
    private boolean simEnded = false;
    private ActionCounter actionCounter = new ActionCounter();
    //private HashMap<Integer, HashMap<Agent, List<Percept>>> perceptBackup = new HashMap<>();

    /**
     * Holds configured agent data.
     */
    private static class AgentConf {
        String name;
        String entity;
        String team;
        String className;

        AgentConf(String name, String entity, String team, String className) {
            this.name = name;
            this.entity = entity;
            this.team = team;
            this.className = className;
        }
    }

    private EnvironmentInterface eis;
    private final List<AgentConf> agentConfigurations = new Vector<>();
    private Map<String, Agent> agents = new HashMap<>();
    private int step = 0;

    /**
     * Create a new scheduler based on the given configuration file
     *
     * @param path path to a java agents configuration file
     */
    Scheduler(String path) {
        parseConfig(path);
    }

    /**
     * Parses the java agents config.
     *
     * @param path the path to the config
     */
    private void parseConfig(String path) {
        try {
            var config = new JSONObject(new String(Files.readAllBytes(Paths.get(path, "javaagentsconfig.json"))));
            var agents = config.optJSONArray("agents");
            if (agents != null) {
                for (int i = 0; i < agents.length(); i++) {
                    var agentBlock = agents.getJSONObject(i);
                    var count = agentBlock.getInt("count");
                    var startIndex = agentBlock.getInt("start-index");
                    var agentPrefix = agentBlock.getString("agent-prefix");
                    var entityPrefix = agentBlock.getString("entity-prefix");
                    var team = agentBlock.getString("team");
                    var agentClass = agentBlock.getString("class");

                    for (int index = startIndex; index < startIndex + count; index++) {
                        agentConfigurations.add(
                                new AgentConf(agentPrefix + index, entityPrefix + index, team, agentClass));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects to an Environment Interface
     *
     * @param ei the interface to connect to
     */
    void setEnvironment(EnvironmentInterface ei) {
        this.eis = ei;
        //HorseRider.inform(TAG, "+++++ Environment set up: " + eis + " ++++++++");
        MailService mailService = new MailService();
        for (AgentConf agentConf : agentConfigurations) {
            /*try {
                makeLogFor(agentConf.name);
            } catch (IOException e) {
                HorseRider.yell(TAG, "setEnvironment: " + agentConf.name + " Failed to make logfile.", e);
            }*/
            Agent agent = null;
            if ("FBAgent".equals(agentConf.className)) {
                agent = new FBAgent(agentConf.name, mailService);
            } else {
                System.out.println("Unknown agent type/class " + agentConf.className);
            }
            if (agent == null) continue;

            mailService.registerAgent(agent, agentConf.team);

            try {
                ei.registerAgent(agent.getName());
            } catch (AgentException e) {
                e.printStackTrace();
            }

            try {
                ei.associateEntity(agent.getName(), agentConf.entity);
                System.out.println("associated agent \"" + agent.getName() + "\" with entity \"" + agentConf.entity + "\"");
            } catch (RelationException e) {
                e.printStackTrace();
            }

            ei.attachAgentListener(agent.getName(), this);
            agents.put(agentConf.name, agent);
        }
        ei.attachEnvironmentListener(this);
    }

    /**
     * resets an Environment stuff
     */
    void resetEnvironment(int match) {
        //HorseRider.inform(TAG, "+++++ Environment set up: " + eis + " ++++++++");
        MailService mailService = new MailService();
        agents = new HashMap<>();
        //perceptBackup = new HashMap<>();
        score = 0;
        step = 0;
        simEnded = false;
        for (AgentConf agentConf : agentConfigurations) {
            try {
                makeLogFor(match, agentConf.name);
            } catch (IOException e) {
                HorseRider.yell(TAG, "setEnvironment: " + agentConf.name + " Failed to make logfile.", e);
            }
            Agent agent = null;
            if ("FBAgent".equals(agentConf.className)) {
                agent = new FBAgent(agentConf.name, mailService);
            } else {
                System.out.println("Unknown agent type/class " + agentConf.className);
            }
            if (agent == null) continue;

            mailService.registerAgent(agent, agentConf.team);
            agents.put(agentConf.name, agent);
        }
    }

    /**
     * Steps all agents and relevant infrastructure.
     *
     * @return end of sim
     */
    boolean step() {


        // retrieve percepts for all agents
        // HorseRider.inform(TAG, "++++++++++++++++++++ step try" + step + " ++++++++++++++++++++++");
        // HorseRider.inform(TAG, "++++++++++++++++++++ agents " + agents + " ++++++++++++++++++++++");
        List<Agent> newPerceptAgents = new Vector<>();
        HashMap<Agent, List<Percept>> stepPercepts = new HashMap<>();

        agents.values().forEach(agent -> {
            List<Percept> percepts = new Vector<>();
            try {
                Collection<Collection<Percept>> perceptsCol = eis.getAllPercepts(agent.getName()).values();
                perceptsCol.forEach(percepts::addAll);
                if (!percepts.isEmpty()) {
                    stepPercepts.putIfAbsent(agent, new Vector<>());
                    stepPercepts.get(agent).addAll(percepts);
                    newPerceptAgents.add(agent);
                    agent.addPercepts(percepts);
                    HorseRider.challenge(TAG, "agent " + agent.getName() + " : " + perceptsCol);
                }
            } catch (PerceiveException e) {
                System.out.println("PerceiveException - No percepts for " + agent.getName() + "/n " + e);
            }

            //HorseRider.inform(TAG, "### set percepts for " + ag + "("+ percepts +")");
        });

        if (newPerceptAgents.size() > 0) {
            HorseRider.inform(TAG, "++++++++++++++++++++ cycle " + step + " --- agents with percepts: " + newPerceptAgents.size() + " ++++++++++++++++++++++");
        }

        // step all agents which have new percepts
        // newPerceptAgents.forEach(agent -> {
        agents.values().forEach(agent -> {
            boolean requestAction = false;
            for (Percept percept : agent.getPercepts()) {
                if (percept.getName().equals("step")) {
                    int pStep = Integer.parseInt(percept.getParameters().get(0).toString());
                    if (pStep > step) {
                        step = pStep;
                        System.out.println("STEP " + step + " score " + score + " actions: " + this.actionCounter.toString());
                        this.actionCounter.reset();
                        /*if (perceptBackup.containsKey(step - 1)) {
                            perceptBackup.get(step - 1).forEach((keyAgent, percepts) ->
                                    percepts.forEach(debugPercept ->
                                            HorseRider.inquire(TAG, "Percept debug " + (step - 1) + " " + keyAgent.getName() + ": " + debugPercept.toProlog())));
                        }*/
                    }
                    break;
                }
            }
            for (Percept percept : agent.getPercepts()) {
                if (percept.getName().equals("score")) {
                    int pScore = Integer.parseInt(percept.getParameters().get(0).toString());
                    if (pScore > score) {
                        score = pScore;
                    }
                    break;
                }
            }
            for (Percept percept : agent.getPercepts()) {
                if (percept.getName().equals("requestAction")) {
                    requestAction = true;
                    //System.out.println("AGENT " + agent.getName() + " in step " + step + " requestAction ");
                    break;
                }
            }
            for (Percept percept : agent.getPercepts()) {
                if (percept.getName().equals("simEnd")) {
                    System.out.println(" ---------  SIM END -----------");
                    simEnded = true;
                    break;
                    /* while (true) {try {
                            Thread.sleep(100); // wait a bit in case no agents have been executed
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }*/
                }
            }
            /*if (stepPercepts.containsKey(agent)) {
                perceptBackup.putIfAbsent(step, new HashMap<>());
                perceptBackup.get(step).putIfAbsent(agent, new Vector<>());
                perceptBackup.get(step).get(agent).addAll(stepPercepts.get(agent));
            }*/
            if (requestAction &&                        // Sim wants agent to run
                    !stepPercepts.containsKey(agent) && // in last check no new percepts were found
                    !agent.isRunning()) {               // agent is not already running
                agent.setRunning(true);
                Runnable runnable = () -> {
                    long startTime = System.currentTimeMillis();
                    StepAction action = null;
                    try {
                        action = agent.step();
                    } catch (Exception e) {
                        HorseRider.yell(TAG, "step: agent fail " + agent, e);
                    }
                    agent.setRunning(false);
                    if (action != null) {
                        if (action.getStep() != step) {
                            System.out.println("Could not perform action - already on new step" + action + " for " + agent.getName());
                            HorseRider.yell(TAG, "agent " + agent.getName() + " submitting action too late " + action + " (in step: " + step + ")");
                        } else {
                            if (action.getAction() != null) {
                                try {
                                    HorseRider.inform(TAG, "agent " + agent.getName() + " submitting action after " + (System.currentTimeMillis() - startTime) + " " + action.getAction().toProlog());
                                    eis.performAction(agent.getName(), action.getAction());
                                    logAction(action);
                                } catch (ActException e) {
                                    System.out.println("Could not perform action " + action.getAction().getName() + " for " + agent.getName());
                                    HorseRider.yell(TAG, "agent " + agent.getName() + " submitting action error " + action.getAction().getName(), e);
                                }
                            }
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        if (newPerceptAgents.size() == 0) try {
            Thread.sleep(20); // wait a bit in case no agents have been executed
        } catch (InterruptedException ignored) {
        }
        return !simEnded;
    }

    private void logAction(StepAction action) {
        FBGoal goal = action.getGoal();
        if (goal instanceof FBGoalDoNothing) {
            this.actionCounter.FBGoalDoNothing++;
        } else if (goal instanceof FBGoalGoSubmit) {
            this.actionCounter.FBGoalGoSubmit++;
        } else if (goal instanceof FBGoalSplit) {
            this.actionCounter.FBGoalSplit++;
        } else if (goal instanceof FBGoalDodge) {
            this.actionCounter.FBGoalDodge++;
        } else if (goal instanceof FBGoalGoConnect) {
            this.actionCounter.FBGoalGoConnect++;
        } else if (goal instanceof FBGoalGoGetTask) {
            this.actionCounter.FBGoalGoGetTask++;
        } else if (goal instanceof FBGoalHoard) {
            this.actionCounter.FBGoalHoard++;
        } else if (goal instanceof FBGoalRoam) {
            this.actionCounter.FBGoalRoam++;
        } else if (goal instanceof FBGoalHamperEnemy) {
            this.actionCounter.FBGoalHamperEnemy++;
        } else if (goal instanceof FBGoalGoNearSubmit) {
            this.actionCounter.FBGoalGoNearSubmit++;
        } else if (goal instanceof FBGoalDig) {
            this.actionCounter.FBGoalDig++;
        }
    }

    @Override
    public void handlePercept(String agent, Percept percept) {
        throw new NotImplementedException(TAG + " handlePercept: " + agent + " ");
    }

    @Override
    public void handleStateChange(EnvironmentState newState) {
    }

    @Override
    public void handleFreeEntity(String entity, Collection<String> agents) {
    }

    @Override
    public void handleDeletedEntity(String entity, Collection<String> agents) {
    }

    @Override
    public void handleNewEntity(String entity) {
    }

    private class ActionCounter {
        public int FBGoalDoNothing = 0;
        public int FBGoalGoSubmit = 0;
        public int FBGoalSplit = 0;
        public int FBGoalDodge = 0;
        public int FBGoalGoConnect = 0;
        public int FBGoalGoGetTask = 0;
        public int FBGoalHoard = 0;
        public int FBGoalRoam = 0;
        public int FBGoalHamperEnemy = 0;
        public int FBGoalGoNearSubmit = 0;
        public int FBGoalDig = 0;

        public void reset() {
            FBGoalDoNothing = 0;
            FBGoalGoSubmit = 0;
            FBGoalSplit = 0;
            FBGoalDodge = 0;
            FBGoalGoConnect = 0;
            FBGoalGoGetTask = 0;
            FBGoalHoard = 0;
            FBGoalRoam = 0;
            FBGoalHamperEnemy = 0;
            FBGoalGoNearSubmit = 0;
            FBGoalDig = 0;
        }

        @Override
        public String toString() {
            return  "S:" + FBGoalGoSubmit +
                    " Sp:" + FBGoalSplit +
                    " D:" + FBGoalDodge +
                    " C:" + FBGoalGoConnect +
                    " T:" + FBGoalGoGetTask +
                    " H:" + FBGoalHoard +
                    " R:" + FBGoalRoam +
                    " HE:" + FBGoalHamperEnemy +
                    " GNS:" + FBGoalGoNearSubmit +
                    " Dig:" + FBGoalDig +
                    " Not:" + FBGoalDoNothing;
        }
    }
}
