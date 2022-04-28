package fitBut.agents;

import eis.iilang.Percept;
import fitBut.MailService;
import fitBut.utils.StepAction;

import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An abstract Java agent.
 */
public abstract class Agent {

    String name;
    MailService mailbox;
    private List<Percept> percepts = newPerceptsList();
    private boolean running = false;

    /**
     * Constructor
     *
     * @param name    the agent's name
     * @param mailbox the mail facility
     */
    Agent(String name, MailService mailbox) {
        this.name = name;
        this.mailbox = mailbox;
    }

    Agent() {
    }

    /**
     * Called for each step.
     * @return
     */
    public abstract StepAction step();

    /**
     * @return the name of the agent
     */
    public String getName() {
        return name;
    }

    /**
     * Sends a percept as a message to the given agent.
     * The receiver agent may fetch the message the next time it is stepped.
     *
     * @param message  the message to deliver
     * @param receiver the receiving agent
     * @param sender   the agent sending the message
     */
    @SuppressWarnings("unused")
    protected void sendMessage(Percept message, String receiver, String sender) {
        mailbox.sendMessage(message, receiver, sender);
    }

    /**
     * Broadcasts a message to the entire team.
     *
     * @param message the message to broadcast
     * @param sender  the agent sending the message
     */
    @SuppressWarnings("unused")
    void broadcast(Percept message, String sender) {
        mailbox.broadcast(message, sender);
    }

    /**
     * Called if another agent sent a message to this agent; so technically this is part of another agent's step method.
     *
     * @param message the message that was sent
     * @param sender  name of the agent who sent the message
     */
    public abstract void handleMessage(Percept message, String sender);

    /**
     * Sets the percepts for this agent. Should only be called from the outside.
     *
     * @param percepts the new percepts for this agent.
     */
    public void setPercepts(List<Percept> percepts) {
        this.percepts = percepts;
    }

    /**
     * Prints a message to std out prefixed with the agent's name.
     *
     * @param message the message to say
     */
    @SuppressWarnings("unused")
    void say(String message) {
        System.out.println("[ " + name + " ]  " + message);
    }


    /**
     * Returns a list of this agent's percepts. Percepts are set by the scheduler
     * each time before the step() method is called.
     * Percepts are cleared before each step, so relevant information needs to be stored somewhere else
     * by the agent.
     *
     * @return a list of all new percepts for the current step
     */
    public List<Percept> getPercepts() {
        return percepts;
    }

    public List<Percept> popPercepts() {
        List<Percept> percepts_cpy = Collections.synchronizedList(new Vector<>());
        while (!percepts.isEmpty()) {
            percepts_cpy.add(percepts.get(0));
            percepts.remove(0);
        }
        return percepts_cpy;
    }

    List<Percept> newPerceptsList() {
        return new CopyOnWriteArrayList<>(new Vector<>());
    }

    List<Percept> clearPercepts() {
        List<Percept> restPercepts = percepts;
        this.percepts = newPerceptsList();
        return restPercepts;
    }


    public abstract boolean isStepTimedOut(int step);

    public void addPercepts(List<Percept> percepts) {
        this.percepts.addAll(percepts);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean b) {
        running = b;
    }
}
