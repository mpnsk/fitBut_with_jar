package fitBut.fbEnvironment;

import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBCells.FBCell;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.FBCells.objects.FBCellObject;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbMultiagent.FBGroup;
import fitBut.utils.Point;
import fitBut.utils.exceptions.ShouldNeverHappen;
import fitBut.utils.logging.HorseRider;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/

public class FBGroupMap extends FBMap {

    private static final String TAG = "FBGroupMap";
    private final FBGroup group;

    public FBGroupMap(FBGroup group) {
        super(group.getName());
        this.group = group;
    }

    // using for retrieving agent
    // todo: use some other useful method?
    private FBCell getAgentNode(Point point) { //todo: optimize based on step?
        HashSet<FBCell> nodes = new HashSet<>();

        ConcurrentHashMap<FBAgent, Point> members = group.getMembers();
        for (FBAgent member : members.keySet()) {
            Point memberDisplacement = members.get(member);
            Point memberPoint = new Point(point);
            memberPoint.translate(-memberDisplacement.x, -memberDisplacement.y);
            FBCell cell = member.getLocalMap().getNode(memberPoint);
            if (cell != null && cell.containsType(FBObjectType.__FBAgent)) {
                nodes.add(cell);
            }
        }
        FBCell node = null;
        int latest = -1;
        for (FBCell memberNode : nodes) {
            if (memberNode != null && memberNode.getLatestStepMark() > latest) {
                latest = memberNode.getLatestStepMark();
                node = memberNode;
            }
        }
        return node;
    }

    FBCell getNode(Point point) { //todo: optimize based on step?

        HashSet<FBCell> nodes = new HashSet<>();
        ConcurrentHashMap<FBAgent, Point> members = group.getMembers();
        for (FBAgent member : members.keySet()) {
            Point memberDisplacement = members.get(member);
            Point memberPoint = new Point(point);
            memberPoint.translate(-memberDisplacement.x, -memberDisplacement.y);
            nodes.add(member.getLocalMap().getNode(memberPoint));
        }
        FBCell node = null;
        int latest = -1;
        for (FBCell memberNode : nodes) {
            if (memberNode != null) {
                if (memberNode.getLatestStepMark() > latest) { //newer node
                    latest = memberNode.getLatestStepMark();
                    node = memberNode;
                } else if (memberNode.getLatestStepMark() == latest) { //node of same age
                    if (node == null) {
                        //throw new ShouldNeverHappen(TAG + " getNode: node age -1 ?" + getName() + " " + latest);
                       continue;
                    }
                    FBBlockObject indexedBlock = node.getBlockObject();
                    FBBlockObject newBlock = memberNode.getBlockObject();
                    if (indexedBlock != null && newBlock != null) {
                        indexedBlock.mergeAttached(newBlock, latest);
                    } else {
                        if (!(indexedBlock == null && newBlock == null) && latest == latestStep()) {
                            HorseRider.warn(TAG, "getNode: " + getName() + " same age but different content! at:" + point + "at step:" + latest + "\n" +
                                    "1. node:" + node.getOwner() + node.getLatestCellContent() + "\n" +
                                    "2. node:" + memberNode.getOwner() + memberNode.getLatestCellContent());
                        }
                    }
                }
            }
        }
        return node;

    }

    @Override
    public boolean hasCellAt(Point point) {
        ConcurrentHashMap<FBAgent, Point> members = group.getMembers();
        for (FBAgent member : members.keySet()) {
            Point memberDisplacement = members.get(member);
            Point memberPoint = new Point(point);
            memberPoint.translate(-memberDisplacement.x, -memberDisplacement.y);
            if (member.getLocalMap().hasCellAt(memberPoint)) {
                return true;
            }
        }
        return false;
    }

    @Override
    void recalculateForNewLimits() {
        ConcurrentHashMap<FBAgent, Point> members = group.getMembers();
        for (FBAgent member : members.keySet()) {
            member.getLocalMap().checkLimitChange();
        }
    }

    @Override
    public boolean isTraversableAt(Point at) {
        return getNode(at) != null && getNode(at).isTraversable();
    }

    /*@Override
    public boolean isOutside(Point point) {
        ConcurrentHashMap<FBAgent, Point> members = group.getMembers();
        for (FBAgent member : members.keySet()) {
            Point memberDisplacement = members.get(member);
            Point memberPoint = new Point(point);
            memberPoint.translate(-memberDisplacement.x, -memberDisplacement.y);
            if (member.getLocalMap().isOutside(memberPoint)) {
                return true;
            }
        }
        return false;
    }*/

    /**
     * Creates snapshot of map - dumping history a flattening all to last step
     */
    @Override
    public FBMapPlain getMapSnapshot(int stepLimit) {
        //checkLimitChange(); //todo: reeval
        FBMapPlain map = new FBMapPlain(this.getName() + " snapshot");
        for (FBAgent agent : group.getMembers().keySet()) {                 //for all agents
            Point memberDisplacement = group.getMembers().get(agent);
            Point memberPoint = new Point(0, 0);
            memberPoint.add(memberDisplacement);
            //map.importBorder(agent.getLocalMap().getBorder(), memberPoint);
            for (Point point : agent.getLocalMap().getCellList()) {                  //for every cell
                Point groupPoint = new Point(point).add(memberDisplacement).getLimited();
                if (!map.hasCellAt(groupPoint) && getNode(groupPoint).getLatestStepMark() >= stepLimit) {
                    map.insertEmptyVisionCells(0, groupPoint, getNode(groupPoint).getLatestStepMark());// add cell
                    ConcurrentHashMap<FBObjectType, FBCellObject> cellContent = getNode(groupPoint).getLatestCellContent();
                    int nodeDate = getNode(groupPoint).getLatestStepMark();
                    if (cellContent.containsKey(FBObjectType.__FBEntity_Friend) &&
                            getAgentNode(groupPoint) != null) {
                        // if cell has friend and agent at once get only agent
                        cellContent = getAgentNode(groupPoint).getLatestCellContent();
                        nodeDate = getAgentNode(groupPoint).getLatestStepMark();
                    }
                    for (FBCellObject cellObject : cellContent.values()) {
                        map.addCellObject(groupPoint, cellObject, nodeDate);
                    }
                }
            }
        }
        return map;
    }

    @Override
    public int getXMin() {
        final int[] min = {0};
        group.getMembers().forEach((member, displacement) ->
                min[0] = Math.min(min[0], member.getLocalMap().getXMin() + displacement.x));
        return min[0];
    }

    @Override
    public int getXMax() {
        final int[] max = {0};
        group.getMembers().forEach((member, displacement) ->
                max[0] = Math.max(max[0], member.getLocalMap().getXMax() + displacement.x));
        return max[0];
    }

    @Override
    public int getYMin() {
        final int[] min = {0};
        group.getMembers().forEach((member, displacement) ->
                min[0] = Math.min(min[0], member.getLocalMap().getYMin() + displacement.y));
        return min[0];
    }

    @Override
    public int getYMax() {
        final int[] max = {0};
        group.getMembers().forEach((member, displacement) ->
                max[0] = Math.max(max[0], member.getLocalMap().getYMax() + displacement.y));
        return max[0];
    }
}
