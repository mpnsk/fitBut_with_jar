package fitBut.fbReasoningModule.fbGoals.fbMultiGoals.utils;

import fitBut.agents.FBAgent;
import fitBut.fbReasoningModule.fbGoals.FBGoal;
import fitBut.fbReasoningModule.fbGoals.utils.PlanCell;
import fitBut.utils.Point;
import fitBut.utils.PointAndDir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author : Vaclav Uhlir
 * @since : 11/10/2019
 **/

public class PlanningStruct {
    public FBAgent agent;
    public Set<Point> bodyCollisionIgnoreList;
    public FBGoal agentGoal;
    public ArrayList<PlanCell> open;
    public HashMap<PointAndDir, PlanCell> indexed;
    public int preferredDirection;
}
