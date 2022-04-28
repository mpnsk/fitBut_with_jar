package fitBut.fbReasoningModule.fbGoals.utils;

import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.FBBody;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbMultiagent.FBTask;
import fitBut.fbPerceptionModule.data.BlockType;
import fitBut.utils.Point;
import fitBut.utils.PointAndDir;
import fitBut.utils.PointPointAndDir;

import java.util.*;

/**
 * @author : Vaclav Uhlir
 * @since : 8.10.2019
 **/
public class TaskMatch {

    private static final String TAG = "TaskMatch";

    //todo: move to its own decision-type class
    public static ArrayList<TaskMatchStructure> completeness(FBTask task, FBAgent agent) {
        ArrayList<TaskMatchStructure> matches = new ArrayList<>();
        HashMap<Point, FBBlockObject> agentBody = agent.getBody().getBodyBlocks();
        if (agentBody.isEmpty()) { // agent doesn't have body
            return matches;
        }

        HashMap<Point, BlockType> taskBody = task.getTaskBody();
        HashSet<PointPointAndDir> indexed = new HashSet<>(); //TaskPoint, BodyPointZero, body rotation

        for (Point taskPoint : taskBody.keySet()) { // for all point in task
            BlockType taskBlockType = taskBody.get(taskPoint);
            for (Point bodyPoint : agentBody.keySet()) { // for all body cells
                BlockType bodyBlockType = agentBody.get(bodyPoint).getBlockType();
                if (bodyBlockType.equals(taskBlockType)) {     // found same block!
                    if (isNotValidFortAcceptedTask(task, agent, taskPoint, bodyPoint)) continue;
                    //lets check if neighbors correspond
                    for (Rotation rotation : Rotation.fourDirections()) {
                        Point bodyPointZero = taskPoint.diff(bodyPoint.getRotated(rotation));
                        if (taskPoint.equals(Point.unitY()) && !bodyPointZero.equals(Point.zero()))
                            continue; // first point not positioned
                        PointPointAndDir matchPointIndex = new PointPointAndDir(taskPoint, bodyPointZero, rotation.clockDirection());

                        if (!indexed.add(matchPointIndex)) continue; //index had point ->leave

                        TaskMatchStructure hit = new TaskMatchStructure(task, agent, taskPoint, bodyPoint, rotation.mirrored());
                        if (checkLinkedNeighbours(agent, task, bodyPoint, bodyPointZero, rotation, taskBody, taskPoint, hit, indexed)) {
                            hit.setHitRatioTo(taskBody.size());
                            matches.add(hit);
                        }
                    }
                } /*else {
                        if (bodyBlockType.equals(taskBlockType)) {
                            HorseRider.challenge(TAG, "completeness: " + agent + " wrong accepted task hit \n" +
                                    "is first point: " + (taskPoint.distance(new Point(0, 1)) == 0 && bodyPoint.distance(Point.zero()) == 1) + "\n" +
                                    "task mach: " + task.getName() + " /" + agent.getAgentInfo().getAcceptedTask() + " -> " +
                                    task.getName().equals(agent.getAgentInfo().getAcceptedTask()));
                        }
                    }*/

            }
        }
        return matches;
    }

    private static boolean isNotValidFortAcceptedTask(FBTask task, FBAgent agent, Point taskPoint, Point bodyPoint) {
        return !(!taskPoint.equals(Point.unitY()) ||   // not first task point
                (bodyPoint.distance(Point.zero()) == 1 &&            // body first point
                        // or must correspond with accepted task
                        task.getName().equals(agent.getAgentInfo().getAcceptedTask())));
    }

    public static ArrayList<TaskMatchStructure> taskMatch(FBTask task, FBAgent agent) {
        ArrayList<TaskMatchStructure> matches = new ArrayList<>();
        HashMap<Point, FBBlockObject> agentBody = agent.getBody().getBodyBlocks();
        if (agentBody.isEmpty()) { // agent doesn't have body
            return matches;
        }

        HashMap<Point, BlockType> taskBody = task.getTaskBody();
        HashSet<PointPointAndDir> indexed = new HashSet<>(); //offset and point

        //for (Point taskPoint : taskBody.keySet()) { // for all point in task
        Point taskPoint = new Point(0, 1);
        BlockType taskBlockType = taskBody.get(taskPoint);
        for (Point bodyPoint : agentBody.keySet()) { // for all body cells
            if (bodyPoint.distance(Point.zero()) != 1) continue;
            BlockType bodyBlockType = agentBody.get(bodyPoint).getBlockType();

            if (bodyBlockType.equals(taskBlockType)) {
                //lets check if neighbors correspond
                for (Rotation rotation : Rotation.fourDirections()) {
                    Point bodyPointZero = bodyPoint.diff(bodyPoint.getRotated(rotation));
                    PointPointAndDir matchPointIndex = new PointPointAndDir(taskPoint, bodyPointZero, rotation.clockDirection());

                    if (!indexed.add(matchPointIndex)) continue; //index had point ->leave

                    TaskMatchStructure hit = new TaskMatchStructure(task, agent, taskPoint, bodyPoint, rotation.mirrored());
                    if (checkLinkedNeighbours(agent, task, bodyPoint, bodyPointZero, rotation, taskBody, taskPoint, hit, indexed)) {
                        hit.setHitRatioTo(taskBody.size());
                        matches.add(hit);
                    }
                }
            }
        }
        return matches;
    }

    //                  checkLinkedNeighbours(agent, task, bodyPoint, bodyPointZero, rotation, taskBody, taskPoint, hit, indexed);
    private static boolean checkLinkedNeighbours(FBAgent agent, FBTask task, Point bodyPoint, Point bodyPointZero, Rotation rotation,
                                                 HashMap<Point, BlockType> taskBody, Point taskPoint,
                                                 TaskMatchStructure hit, HashSet<PointPointAndDir> indexed) {
        FBBody body = agent.getBody();
        for (Point connectedBlock : body.getLinked(bodyPoint)) {
            Point nextTaskPoint = taskPoint.sum(connectedBlock.diff(bodyPoint).getRotated(rotation));

            if (taskPoint.equals(Point.unitY())) {
                if (!bodyPointZero.equals(Point.zero())) {
                    return false; // first point not positioned
                }else{
                    hit.setMaster(true);
                }
            }
            if (!indexed.add(new PointPointAndDir(nextTaskPoint, bodyPointZero, rotation.clockDirection())))
                continue; //TaskPoint, BodyPointZero, body rotation

            if (taskBody.get(nextTaskPoint) != null) {
                if (body.getBodyBlocks().get(connectedBlock) == null) {
                    return false; // linked null block on task (agent in task)
                }else if (taskBody.get(nextTaskPoint).equals(body.getBodyBlocks().get(connectedBlock).getBlockType())) {
                    if (isNotValidFortAcceptedTask(task, agent, nextTaskPoint, connectedBlock)) return false;
                    hit.addHit(nextTaskPoint);
                    if (!checkLinkedNeighbours(agent, task, connectedBlock, bodyPointZero, rotation, taskBody, nextTaskPoint,
                            hit, indexed)) return false;
                }
            }
        }
        return true;
    }

    public static class TaskMatchStructure {
        private final FBTask task;
        private final FBAgent agent;
        private final Point taskPoint;
        private final Point bodyPoint;
        private final Rotation rotation;
        private final HashSet<Point> hits;
        private double hitRatio;
        private boolean master = false;

        TaskMatchStructure(FBTask task, FBAgent agent, Point taskPoint, Point bodyPoint, Rotation rotation) {
            this.task = task;
            this.agent = agent;
            this.taskPoint = taskPoint;
            this.bodyPoint = bodyPoint;
            this.rotation = rotation;
            this.hits = new HashSet<>();
            this.hits.add(taskPoint);
        }

        void addHit(Point hitPoint) {
            this.hits.add(hitPoint);
        }

        public int getCount() {
            return hits.size();
        }

        void setHitRatioTo(int hitRatioTo) {
            this.hitRatio = (double) getCount() / (double) hitRatioTo;
        }

        public double getHitRatio() {
            return hitRatio;
        }

        public FBTask getTask() {
            return this.task;
        }

        public Rotation getRotation() {
            return rotation;
        }

        public FBAgent getAgent() {
            return agent;
        }

        public HashSet<Point> getHits() {
            return this.hits;
        }

        public Point bodyEquivalentOf(Point taskPoint) {
            return bodyPoint.sum(taskPoint.diff(this.taskPoint).getRotated(rotation));
        }

        public Point taskEquivalentOf(Point bodyPoint) {
            return this.taskPoint.sum(bodyPoint.diff(this.bodyPoint).getRotated(rotation.mirrored()));
        }

        public int getNameHash() {
            return (task.getName() + agent.getName()).hashCode();
        }

        @Override
        public String toString() {
            return task.getName() + " " + hitRatio;
        }

        public void setMaster(boolean master) {
            this.master = master;
        }

        public boolean isMaster() {
            return master;
        }

        public int getReward() {
            return task.getReward();
        }
    }
}
