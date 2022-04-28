package fitBut;

import fitBut.agents.FBAgent;
import fitBut.fbMultiagent.FBGroup;
import fitBut.fbMultiagent.FBRegister;
import fitBut.utils.logging.HorseRider;
import eis.iilang.Percept;
import fitBut.agents.Agent;

import java.util.*;
import java.util.logging.Logger;

/**
 * A simple agentList for agents that forwards messages.
 */
public class MailService {

    private static final String TAG = "MailService";
    private Map<String, Agent> agentList = new HashMap<>();
    private Set<FBGroup> groupList = new HashSet<>();
    private Map<String, List<Agent>> agentsByTeam = new HashMap<>();
    private Map<String, String> teamForAgent = new HashMap<>();
    private Logger logger = Logger.getLogger("agents");


    //private FBRegister fbRegister = new FBRegister();

    /**
     * Registers an agent with this mail service. The agent will now receive messages.
     *
     * @param agent the agent to agentList
     * @param team  the agent's team (needed for broadcasts)
     */
    public void registerAgent(Agent agent, String team) {
        agentList.put(agent.getName(), agent);
        agentsByTeam.putIfAbsent(team, new Vector<>());
        agentsByTeam.get(team).add(agent);
        teamForAgent.put(agent.getName(), team);
        if (agent instanceof FBAgent) {
            //FBRegister.registerNewAgent((FBAgent) agent);
            groupList.add(((FBAgent) agent).getGroup());
        }
    }

    /**
     * Adds a message to this mailbox.
     *
     * @param message the message to add
     * @param to      the receiving agent
     * @param from    the agent sending the message
     */
    public void sendMessage(Percept message, String to, String from) {

        Agent recipient = agentList.get(to);

        if (recipient == null) {
            logger.warning("Cannot deliver message to " + to + "; unknown target,");
        } else {
            recipient.handleMessage(message, from);
        }
    }

    /**
     * Sends a message to all agents of the sender's team (except the sender).
     *
     * @param message the message to broadcast
     * @param sender  the sending agent
     */
    public void broadcast(Percept message, String sender) {
        agentsByTeam.get(teamForAgent.get(sender)).stream()
                .map(Agent::getName)
                .filter(ag -> !ag.equals(sender))
                .forEach(ag -> sendMessage(message, ag, sender));
    }

    public void reportIn(FBAgent fbAgent, int step) {
        if (FBRegister.reportIn(fbAgent, step)) {
            HorseRider.challenge(TAG, "reportIn: " + fbAgent.getName() + " starting decisions");
            for (FBGroup group : groupList) {    // todo: runs decision only if !all! agents report in! what about some backup?
                if (!group.isActive() || !group.getGroupFounder().getSimInfo().isSimStarted()) {
                    continue;
                }
                Runnable runnable =
                        () -> group.runDecisions(step);
                Thread thread = new Thread(runnable);
                thread.start();
            }
        }
    }
}