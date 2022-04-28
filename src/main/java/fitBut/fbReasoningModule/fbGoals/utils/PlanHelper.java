package fitBut.fbReasoningModule.fbGoals.utils;

import fitBut.agents.FBAgent;
import fitBut.fbActions.FBAction;
import fitBut.fbActions.FBMove;

import fitBut.fbActions.FBRotate;
import fitBut.fbEnvironment.FBMap;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbReasoningModule.fbPlans.FBPlan;
import fitBut.utils.Point;
import fitBut.utils.PointAndDir;
import fitBut.utils.logging.HorseRider;

import java.util.*;

/**
 * @author : Vaclav Uhlir
 * @since : 11.9.2019
 **/
public final class PlanHelper {


    @SuppressWarnings("unused")
    private static final String TAG = "PlanHelper";

    public static ArrayList<PlanCell> getNewPlanMap() {
        return new ArrayList<>();
    }

    /**
     * generate new neighbours if not present in:
     *
     * @param open               planned cells
     * @param indexed            current gen cells
     * @param originCell         origin cell
     * @param preferredDirection direction change for not movement unity
     */

    public static void generateStepsToOpen(ArrayList<PlanCell> open,
                                           HashMap<PointAndDir, PlanCell> indexed,
                                           PlanCell originCell,
                                           int preferredDirection) {
        generateStepsToOpen(open, indexed, originCell, preferredDirection, true);
    }

    public static void generateStepsToOpen(ArrayList<PlanCell> open,
                                           HashMap<PointAndDir, PlanCell> indexed,
                                           PlanCell originCell,
                                           int preferredDirection,
                                           boolean useRandom) {
        Point at = originCell.getAt();
        int step = originCell.getStep() + 1;


        // generates 4 direction with rotation shift
        ArrayList<Point> points = generateDirections(at, preferredDirection, useRandom);
        // generate 4 dir step
        for (Point neighbourPos : points) {
            PointAndDir pointAndDir = new PointAndDir(neighbourPos, originCell.getHeading());
            //HorseRider.challenge(TAG, "generateStepsToOpen: pointAndDir: "+pointAndDir+" contained: "+indexed.containsKey(pointAndDir));
            if (!(indexed.containsKey(pointAndDir))) { // not in lists
                PlanCell neighbour = setUpPlanCell(originCell, step, neighbourPos, Rotation.NULL, new FBMove(new Point(neighbourPos.x - at.x, neighbourPos.y - at.y).getLimited()));
                indexed.put(pointAndDir, neighbour);
                open.add(neighbour);
            }
        }
        // generate rotations
        for (Rotation rotation : new Rotation[]{Rotation.CW, Rotation.CCW}) { // for both rotations
            PointAndDir pointAndDir = new PointAndDir(new Point(originCell.getAt()), originCell.getHeading().getRotatedHeading(rotation));
            //HorseRider.challenge(TAG, "generateStepsToOpen: pointAndDir: "+pointAndDir+" contained: "+indexed.containsKey(pointAndDir));
            if (!(indexed.containsKey(pointAndDir))) {            // not in list
                PlanCell rotated = setUpPlanCell(originCell, step, originCell.getAt(), rotation, new FBRotate(rotation));
                indexed.put(pointAndDir, rotated);
                open.add(rotated);
            }
        }
        //open.sort(Comparator.comparingInt(PlanCell::getQueueValue));
    }

    private static PlanCell setUpPlanCell(PlanCell originCell, int step, Point at, Rotation rotation, FBAction action) {
        PlanCell newCell = new PlanCell(at);
        newCell.setStep(step);
        newCell.setHeading(originCell.getHeading().getRotatedHeading(rotation));
        newCell.copyPlanOf(originCell.getPlan());
        newCell.appendToActionPlan(action);
        return newCell;
    }

    /**
     * just 4 direction from 0,0
     *
     * @return [-1,0]...
     */
    public static ArrayList<Point> generateDirections() {
        return generateDirections(Point.zero(), 0, false);
    }

    /**
     * get direction with preference direction and random 1shift
     *
     * @param at                 center
     * @param preferredDirection preferred direction
     * @return [at-1,at]...
     */
    public static ArrayList<Point> generateDirections(Point at, int preferredDirection) {
        return generateDirections(at, preferredDirection, true);
    }

    /**
     * get direction with starting point preference direction and random 1shift
     *
     * @param at                 center
     * @param preferredDirection preferred direction
     * @param randomSwap         do random 1shift
     * @return [at-1,at]...
     */
    public static ArrayList<Point> generateDirections(Point at, int preferredDirection, boolean randomSwap) {
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int x = (i - 2) % 2;
            int y = ((i - 1) % 2);
            //HorseRider.warn(TAG, "generateStepsToOpen: x:"+x+" y:"+y);
            Point neighbourPos = new Point(at.x + x, at.y + y).getLimited();
            points.add(neighbourPos);
        }

        //HorseRider.warn(TAG, "generateStepsToOpen: dir: " + preferredDirection + " at: " + at + " points: " + points);
        Collections.rotate(points, preferredDirection);
        if (preferredDirection < 0) {
            Collections.reverse(points);
        }
        //HorseRider.warn(TAG, "generateStepsToOpen: dir: " + preferredDirection + " at: " + at + " points: " + points);

        if (randomSwap && new Random().nextBoolean()) { // just sometimes under-shift points to not go in straight lines
            Point point1 = points.remove(0);
            points.add(point1);
        }

        return points;
    }

    public static boolean checkIfCompatible(FBAgent agent1, FBPlan plan1, FBAgent agent2, FBPlan plan2) {
        for (int i = 0; i < Math.max(plan1.size(), plan2.size()); i++) {
            if (!FBMap.mergeAbleLayers(plan1.getFuture(agent1, i), plan2.getFuture(agent2, i))) return false;
        }

        return true;
    }
}