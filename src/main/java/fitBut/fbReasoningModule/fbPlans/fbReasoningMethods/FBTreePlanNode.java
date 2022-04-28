package fitBut.fbReasoningModule.fbPlans.fbReasoningMethods;

import fitBut.fbActions.FBAction;
import fitBut.fbEnvironment.FBMapLayer;

import java.util.HashMap;

/**
 * @author : Vaclav Uhlir
 * @since : 25.9.2019
 **/
@Deprecated
public class FBTreePlanNode {
    private int step;
    private FBMapLayer mapLayer;
    private FBTreePlan tree;
    private FBTreePlanNode parent;
    private FBAction action;
    private HashMap<FBAction,FBTreePlanNode> children = new HashMap<>();

    private FBTreePlanNode(FBTreePlanNode fbTreePlanNode, FBAction action) {
        parent = fbTreePlanNode;
        this.action = action;
        this.step = parent.getStep()+1;
        this.tree = parent.getTree();
    }

    public void addAction(FBAction action) {
        children.put(action, new FBTreePlanNode(this, action));
    }

    public int getStep() {
        return step;
    }

    private FBTreePlan getTree() {
        return tree;
    }

}
