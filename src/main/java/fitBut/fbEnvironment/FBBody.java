package fitBut.fbEnvironment;


import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbPerceptionModule.data.BlockType;
import fitBut.fbReasoningModule.fbGoals.FBGoal;
import fitBut.fbReasoningModule.fbGoals.FBGoalSplit;
import fitBut.fbReasoningModule.fbGoals.utils.PlanHelper;
import fitBut.utils.Point;
import fitBut.utils.logging.HorseRider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FBBody {
    private static final String TAG = "FBBody";
    private HashMap<Point, FBBlockObject> bodyList = new HashMap<>();
    private FBAgent agent;
    private ConcurrentHashMap<Point, HashSet<Point>> links = new ConcurrentHashMap<>();

    public FBBody(FBAgent fbAgent) {
        this.agent = fbAgent;
    }

    public FBBody(FBAgent agent, HashMap<Point, FBBlockObject> newBodyList, ConcurrentHashMap<Point, HashSet<Point>> newLinkList) {
        this.bodyList = newBodyList;
        this.agent = agent;
        this.links = newLinkList;
    }


    /**
     * Generates body for point reservation
     *
     * @param list     original body
     * @param rotation rotation
     * @return body with most of the covered cells
     */
    public static Set<Point> generateRotationBody(Set<Point> list, Rotation rotation) {
        Set<Point> rotationBody = new HashSet<>();

        for (Point origin : list) {
            if (rotationBody.contains(origin)) continue;
            Point from = new Point(origin);
            Point to = from.getRotated(rotation);
            Point path = to.diff(from);
            int magic = Math.max(Math.abs(path.x), Math.abs(path.y));
            rotationBody.add(new Point(from));
            for (int i = 0; i < magic; i++) {
                Point shift = Point.zero();
                if (rotation == Rotation.CW) {
                    //for CW
                    if (from.y < 0 || (from.y == 0 && from.x < 0)) {
                        shift.x++;
                    } else {
                        shift.x--;
                    }
                    if (from.x > 0 || (from.x == 0 && from.y < 0)) {
                        shift.y++;
                    } else {
                        shift.y--;
                    }
                } else {
                    //for CCW
                    if (from.y < 0 || (from.y == 0 && from.x > 0)) {
                        shift.x--;
                    } else {
                        shift.x++;
                    }
                    if (from.x > 0 || (from.x == 0 && from.y > 0)) {
                        shift.y--;
                    } else {
                        shift.y++;
                    }
                }
                rotationBody.add(new Point(from.add(shift)));
            }
        }
        return rotationBody;
    }

    public void addCell(Point source, Point point, FBBlockObject cellObject) {
        bodyList.put(point, cellObject);
        addLinks(source, point);
    }

    private void removeCell(Point point) {
        bodyList.remove(point);
        removeFromLinks(point);
    }

    private void removeFromLinks(Point point) {
        links.forEach((point1, points) -> points.remove(point));
        links.remove(point);
    }

    private void addLinks(Point source, Point point) {
        addLink(source, point);
        addLink(point, source);
    }

    private void addLink(Point source, Point point) {
        HashSet<Point> oldSet = links.getOrDefault(source, new HashSet<>());
        oldSet.add(point);
        links.put(source, oldSet);
    }

    public FBBody disconnectBodyPart(Point from, Point to) {
        removeLink(from, to);
        removeLink(to, from);
        disconnectLinked(to);
        return this;
    }

    private void disconnectLinked(Point to) {
        disconnectCell(to);
        for (Point links : new HashSet<>(getLinked(to))) {
            disconnectBodyPart(to, links);
        }
    }

    private void removeLink(Point from, Point to) {
        if (links.get(from) != null) {
            links.get(from).remove(to);
            if (links.get(from).isEmpty()) links.remove(from);
        }
    }

    public Set<Point> getList() {
        return bodyList.keySet();
    }

    public void checkIntegrity(FBMap map, Point center) {
        HashSet<Point> checked = new HashSet<>();
        checkLinked(Point.zero(), center, map, checked);
        HashSet<Point> isConnectedList = new HashSet<>(bodyList.keySet());
        for (Point point : isConnectedList) {
            //checkSecretlyConnectedBlocks(map, point);
            if (!checked.contains(point)) { // not linked
                checkLinked(Point.zero(), center, map, checked); //todo: remove
                HorseRider.yell(TAG, "checkIntegrity: agent missing body not connected: " + map.getName() + " " + point);
                //throw new ShouldNeverHappen(TAG + "checkIntegrity: " + agent.getName() + "checkIntegrity: agent missing body not connected: " + map.getName() + " " + point);
                //disconnectBodyPart(source, point);
                removeCell(point);
            }
        }

    }

    //todo: debug and use
    public FBGoal checkSecretlyConnectedBlocks(FBMap map, Point blockPoint) {

        FBGoal returnGoal = null;
        for (Point next : PlanHelper.generateDirections()) {
            Point relativeNeighbour = blockPoint.sum(next);
            FBBlockObject block = map.getBlockObjectAt(agent.getLatestLocalPosition().sum(relativeNeighbour));
            if (block != null && block.isNotAttachedTo(agent) && !block.notAttached()) {
                HashSet<Point> walkedOn = new HashSet<>();
                walkedOn.add(relativeNeighbour);
                walkedOn.add(blockPoint);
                if (!isTouchingByProxyOtherAgent(agent, relativeNeighbour, map, walkedOn)) {
                    walkedOn = new HashSet<>();
                    walkedOn.add(relativeNeighbour);
                    walkedOn.add(blockPoint);
                    agent.getBody().addCell(Point.zero(), next, (FBBlockObject) block.getClone());
                    joinConnectedBlocks(agent, relativeNeighbour, map, walkedOn);
                } else { //other agent may be connected lets disconnect to be sure
                    returnGoal = new FBGoalSplit(blockPoint, relativeNeighbour, "hotWireDisconnect");
                }
            }
        }
        return returnGoal;
    }

    private boolean isTouchingByProxyOtherAgent(FBAgent agent, Point blockPoint, FBMap map, HashSet<Point> walkedOn) {
        for (Point next : PlanHelper.generateDirections()) {
            Point relativeNeighbour = blockPoint.sum(next);
            if (walkedOn.contains(relativeNeighbour)) continue;
            walkedOn.add(relativeNeighbour);
            FBBlockObject block = map.getBlockObjectAt(agent.getLatestLocalPosition().sum(relativeNeighbour));
            // not agent's block that is linked
            if (block != null && block.isNotAttachedTo(agent) && !block.notAttached()) {
                if (isTouchingByProxyOtherAgent(agent, relativeNeighbour, map, walkedOn)) { // not connected to entity
                    return true; // connected invalidate all previous
                }
            }
            if (map.getNodeContainsType(agent.getLatestLocalPosition().sum(relativeNeighbour), FBObjectType.__FBEntity_Friend)){// ||
                    //map.getNodeContainsType(agent.getLatestLocalPosition().sum(relativeNeighbour), FBObjectType.__FBEntity_Enemy)) {
                return true;
            }

        }
        return false;
    }

    private void checkLinked(Point source, Point center, FBMap map, HashSet<Point> checked) {
        for (Point point : new HashSet<>(getLinked(source))) {
            if (point.equals(Point.zero()) || checked.contains(point)) continue;

            FBBlockObject onMapBlock = map.getBlockObjectAt(center.sum(point));

            FBBlockObject expected = bodyList.get(point);
            if (expected != null) {
                if (onMapBlock == null) {
                    HorseRider.warn(TAG, "checkIntegrity: agent missing body part: " + map.getName() + " " + point);
                    disconnectBodyPart(source, point);
                    //removeCell(point);
                } else if (onMapBlock.notAttached()) {
                    HorseRider.warn(TAG, "checkIntegrity: agent body part disconnected: " + map.getName() + " " + point);
                    disconnectBodyPart(source, point);
                    //removeCell(point);
                } else {
                    if (onMapBlock.getBlockType().equals(expected.getBlockType())) {
                        onMapBlock.addAttachedTo(this.getAgent());
                        checked.add(point);
                        HorseRider.challenge(TAG, "checkIntegrity: check successful " +
                                onMapBlock + " at " + map.getName() + " " + point);

                        checkLinked(point, center, map, checked);                           // and check linked
                    } else {
                        HorseRider.yell(TAG, "checkIntegrity: failed type at " +
                                onMapBlock + " at " + map.getName() + " " + point +
                                onMapBlock.getBlockType() + " != " + expected.getBlockType());
                    }
                }
            } else {
                HorseRider.yell(TAG, "checkIntegrity: body containing non-block at " + map.getName() + " " + point);
            }
        }
    }

    private void joinConnectedBlocks(FBAgent agent, Point blockPoint, FBMap map, HashSet<Point> walkedOn) {
        for (Point next : PlanHelper.generateDirections()) {
            Point relativeNeighbour = blockPoint.sum(next);
            if (walkedOn.contains(relativeNeighbour)) continue;
            walkedOn.add(relativeNeighbour);
            FBBlockObject block = map.getBlockObjectAt(agent.getLatestLocalPosition().sum(relativeNeighbour));
            // not agent's block that is linked
            if (block != null && block.isNotAttachedTo(agent) && !block.notAttached()) {
                HorseRider.inquire(TAG, "joinConnectedBlocks: " + agent + " Bonus Block! pre: " + agent.getBody()); //TODO: still picking up errors ???
                agent.getBody().addCell(blockPoint, relativeNeighbour, (FBBlockObject) block.getClone());
                HorseRider.inquire(TAG, "joinConnectedBlocks: " + agent + " Bonus Block! pos: " + agent.getBody());
                joinConnectedBlocks(agent, relativeNeighbour, map, walkedOn);
            }
        }
    }

    public FBBody getRotatedBody(Rotation rotation) {
        HashMap<Point, FBBlockObject> newBodyList = new HashMap<>();
        ConcurrentHashMap<Point, HashSet<Point>> newLinkList = new ConcurrentHashMap<>();
        for (Point point : bodyList.keySet()) {
            FBBlockObject block = bodyList.get(point);
            //block.rotateLinks(rotation);
            newBodyList.put(new Point(point).rotate(rotation), block);
        }
        for (Point link : this.links.keySet()) {
            HashSet<Point> linkSet = links.get(link);
            if (linkSet == null) continue;
            HashSet<Point> newLinkSet = new HashSet<>();
            for (Point target : linkSet) {
                newLinkSet.add(target.getRotated(rotation));
            }
            newLinkList.put(link.getRotated(rotation), newLinkSet);
        }
        return new FBBody(agent, newBodyList, newLinkList);
    }

    /**
     * return list of center and body shifted by offset
     *
     * @param offset shift vector
     * @return full body list
     */
    public Set<Point> getShiftedList(Point offset) {
        HashSet<Point> bodyShiftedList = new HashSet<>();
        bodyShiftedList.add(offset);
        for (Point bodyPart : getList()) {
            bodyShiftedList.add(offset.sum(bodyPart).getLimited());
        }
        return bodyShiftedList;
    }

    public Set<Point> getShiftedAndRotatedList(Point offset, Rotation rotation) {
        HashSet<Point> bodyShiftedList = new HashSet<>();
        bodyShiftedList.add(offset);
        for (Point bodyPart : getList()) {
            bodyShiftedList.add(offset.sum(bodyPart.getRotated(rotation)).getLimited());
        }
        return bodyShiftedList;
    }

    public FBAgent getAgent() {
        return agent;
    }

    public void setAgent(FBAgent agent) {
        this.agent = agent;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(agent.getName()).append(" body").append("\n");
        Point min = new Point(0, 0);
        Point max = new Point(0, 0);
        for (Point point : bodyList.keySet()) {
            min = Point.min(min, point);
            max = Point.max(max, point);
        }
        for (int y = min.y; y <= max.y; y++) {
            for (int x = min.x; x <= max.x; x++) {
                FBBlockObject block = bodyList.get(new Point(x, y));
                if (block != null) {
                    string.append(block.getBlockType().getName()).append(" ");
                } else if (x == 0 && y == 0) {
                    string.append("X  ");
                } else {
                    string.append("   ");
                }
            }
            string.append("\n");
        }
        return string.toString();
    }

    public HashMap<Point, FBBlockObject> getBodyBlocks() {
        return bodyList;
    }

    public HashSet<Point> getLinked(Point position) {
        return links.getOrDefault(position, new HashSet<>());
    }

    public HashMap<BlockType, Integer> getAvailableBlocks() {
        HashMap<BlockType, Integer> blocksAvailable = new HashMap<>();
        bodyList.forEach((point, fbBlockObject) ->
                blocksAvailable.put(
                        fbBlockObject.getBlockType(),
                        blocksAvailable.getOrDefault(fbBlockObject.getBlockType(), 0) + 1));
        return blocksAvailable;
    }

    public void dropAll() {
        HorseRider.inquire(TAG, "dropAll: and body" + agent);
        this.links.clear();
        this.bodyList.clear();
    }

    private void disconnectCell(Point vector) {
        bodyList.remove(vector);
    }

    public FBBody getClone() {
        HashMap<Point, FBBlockObject> newBodyList = new HashMap<>();
        ConcurrentHashMap<Point, HashSet<Point>> newLinkList = new ConcurrentHashMap<>();
        for (Point point : new HashSet<>(bodyList.keySet())) {
            FBBlockObject block = bodyList.get(point);
            newBodyList.put(new Point(point), block);
        }
        for (Point link : this.links.keySet()) {
            HashSet<Point> linkSet = links.get(link);
            if (linkSet == null) continue;
            HashSet<Point> newLinkSet = new HashSet<>();
            for (Point target : linkSet) {
                newLinkSet.add(new Point(target));
            }
            newLinkList.put(new Point(link), newLinkSet);
        }
        return new FBBody(agent, newBodyList, newLinkList);
    }
}

