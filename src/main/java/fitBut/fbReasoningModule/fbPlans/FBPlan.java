package fitBut.fbReasoningModule.fbPlans;

import fitBut.agents.FBAgent;
import fitBut.agents.FBSimAgent;
import fitBut.fbActions.FBAction;
import fitBut.fbActions.FBSkip;
import fitBut.fbEnvironment.FBMapLayer;

import java.util.HashMap;
import java.util.LinkedList;

public class FBPlan {

    @SuppressWarnings("unused")
    private static final String TAG = "FBPlan";
    private final LinkedList<FBAction> actionList;
    private final HashMap<Integer, FBSimAgent> future = new HashMap<>();
    private FBAgent agent;

    public FBPlan getCopy() {
        FBPlan duplicate = new FBPlan();
        for (FBAction action : actionList) {
            duplicate.appendAction(action);
        }
        return duplicate;
    }

    @Override
    public String toString() {
        String st = "(" + actionList.size() + ") ";
        for (FBAction action : actionList) {
            st = st.concat(action + " ");
        }
        return st;
    }

    private FBAction getAction(int i) {
        if (i >= actionList.size()) {
            return new FBSkip();
        }
        return (actionList.get(i));
    }

    public FBAction getAction() {
        if(actionList.isEmpty()){
            return new FBSkip();
        }
        return (actionList.getFirst());
    }

    public FBPlan appendAction(FBAction action) {
        actionList.add(action);
        return this;
    }

    public FBPlan() {
        actionList = new LinkedList<>();
    }

    public int size() {
        return actionList.size();
    }

    public FBMapLayer getFuture(FBAgent fbAgent, int i) {
        this.agent = fbAgent;
        if (future.get(i) == null) {
            simulate(i);
        }
        FBSimAgent simFutureAgent = future.get(i);
        if(simFutureAgent!=null) {
            return simFutureAgent.getMapLayer();
        }else{
            return null;
        }
    }

    private void simulate(int i) {
        for (int j = 0; j <= i; j++) {
            if (future.get(j) == null) {
                FBAgent simSource;
                if (j == 0) {
                    simSource = agent;
                } else {
                    simSource = future.get(j - 1);
                }
                future.put(j, getAction(j).simulate(simSource));
            }
        }
    }

    public FBMapLayer getFuture(FBAgent fbAgent) {
        return getFuture(fbAgent, 0);
    }

    public FBAction getLastAction() {
        if (actionList.isEmpty()) return null;
        return actionList.getLast();
    }

    public void removeLastAction() {
        if (actionList.isEmpty()) return;
        actionList.removeLast();
    }

}
