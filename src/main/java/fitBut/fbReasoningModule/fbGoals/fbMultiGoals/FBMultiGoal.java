package fitBut.fbReasoningModule.fbGoals.fbMultiGoals;

import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBMapPlain;
import fitBut.fbReasoningModule.fbGoals.FBGoal;

import java.util.HashMap;
import java.util.Set;


public abstract class FBMultiGoal {

    @SuppressWarnings("unused")
    private static final String TAG = "FBGoal";

    public abstract HashMap<FBAgent, FBGoal> makeGoals(Set<FBAgent> agents, FBMapPlain snapshotMap, int step);

}
