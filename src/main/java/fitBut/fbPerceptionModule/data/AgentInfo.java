package fitBut.fbPerceptionModule.data;

import fitBut.fbMultiagent.FBRegister;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public class AgentInfo {

    private String name = "Unknown";
    private int energy;
    private boolean disabled;
    private String acceptedTask;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getEnergy() {
        return energy;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setAcceptedTask(String acceptedTask) {
        this.acceptedTask = acceptedTask;
    }

    public String getAcceptedTask() {
        return acceptedTask;
    }
}
