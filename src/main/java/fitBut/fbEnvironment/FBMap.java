
package fitBut.fbEnvironment;

import fitBut.fbEnvironment.FBCells.FBCell;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.FBCells.objects.FBCellObject;
import fitBut.fbEnvironment.FBCells.objects.FBDispenserObject;
import fitBut.fbEnvironment.FBCells.objects.FBEntityObject;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbEnvironment.utils.Direction;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.utils.Point;
import fitBut.utils.exceptions.ShouldNeverHappen;
import fitBut.utils.logging.HorseRider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static fitBut.utils.FBConstants.MAP_SPACER_SIZE;

public abstract class FBMap {

    private static final String TAG = "FBMap";

    ConcurrentHashMap<Point, FBCell> cellMap = new ConcurrentHashMap<>(); // hash map point->cell

    @Deprecated
    //Border border = new Border();

    //   PDispensers=new HashMap<Integer,LinkedList<Point>>();
    int PXMin = 0;
    int PXMax = 0;
    int PYMin = 0;
    int PYMax = 0; // map limits
    private String name;
    private int step = -1;
    private short currentLimitVersion = 0;

    public static boolean mergeAble(FBMap reserveMap, FBMap mergeLayer) {
        for (Point point : mergeLayer.getLayerPoints()) {
            if (reserveMap.hasCellAt(point) &&
                    !reserveMap.isTraversableAt(point) && !mergeLayer.isTraversableAt(point) &&
                    reserveMap.getNode(point).isNotSame(mergeLayer.getNode(point))
            ) {
                return false;
            }
        }
        return true;
    }

    public static boolean mergeAbleLayers(FBMapLayer mergeLayer1, FBMapLayer mergeLayer2) {
        for (Point point : mergeLayer2.getLayerPoints()) {
            if (mergeLayer1.getLayerPoints().contains(point) &&
                    !mergeLayer1.isTraversableAt(point) && !mergeLayer2.isTraversableAt(point) &&
                    mergeLayer1.getNode(point).isNotSame(mergeLayer2.getNode(point))
            ) {
                return false;
            }
        }
        return true;
    }

    Set<Point> getLayerPoints() {
        return cellMap.keySet();
    }

    /**
     * checks if cell exists
     *
     * @param position position in question
     * @return true cell if exists
     */
    public abstract boolean hasCellAt(Point position);

    public abstract int getXMin();

    public abstract int getXMax();

    public abstract int getYMin();

    public abstract int getYMax();

    public FBMap(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    abstract FBCell getNode(Point point);

    abstract void recalculateForNewLimits();

    public abstract boolean isTraversableAt(Point at);




    public void checkLimitChange() {
        if (this.currentLimitVersion != Point.LoopLimit.getLoopLimitVersion()) {
            //HorseRider.warn(TAG, "Limit change: from " + currentLimitVersion + " to: " + Point.LoopLimit.getLoopLimitVersion());
            recalculateForNewLimits();
            this.currentLimitVersion = Point.LoopLimit.getLoopLimitVersion();
        }
    }

    void migrateCellMap() {
        //todo: windows not in 0 to 70? check
        PXMin = 0;
        PYMin = 0;
        PXMax = 0;
        PYMax = 0;
        ConcurrentHashMap<Point, FBCell> newCellMap = new ConcurrentHashMap<>();
        for (Point point : cellMap.keySet()) {
            Point newPos = point.getLimited();
            updateXYMinMax(newPos);
            if (newCellMap.containsKey(newPos)) {
                newCellMap.get(newPos).merge(cellMap.get(point));
            } else {
                newCellMap.put(newPos, cellMap.get(point));
            }
        }
        cellMap = newCellMap;
    }

    /**
     * Creates snapshot of map - dumping history and flattening all to last known step
     */
    public abstract FBMapPlain getMapSnapshot(int stepLimit);

    /**
     * get map snapshot and ignore agent
     *
     * @param stepLimit step limit
     * @param agentPos  agent to ignore
     * @param pBodyList body to ignore
     * @return map snapshot
     */
    public FBMapPlain getMapSnapshot(int stepLimit, Point agentPos, Set<Point> pBodyList) {
        if(stepLimit<0)stepLimit=Math.max(0,step+stepLimit);
        Set<Point> bodyList = Point.LoopLimit.limitList(pBodyList);
        FBMapPlain map = new FBMapPlain(this.getName() + " copy");
        //map.getBorder().importBorder(this.getBorder(), new Point(0, 0));
        Point debugPoint = null;
        try {
            for (Point point : cellMap.keySet()) { // for all the cells
                debugPoint = point;
                if (cellMap.get(point)!=null && cellMap.get(point).getLatestStepMark() >= stepLimit) {
                    map.insertEmptyVisionCells(0, point, cellMap.get(point).getLatestStepMark());
                    for (FBCellObject cellObject : this.cellMap.get(point).getLatestCellContent().values()) { // for all object in last pass
                        if (!(bodyList != null && bodyList.contains(point) && cellObject.getObjectType().equals(FBObjectType.__FBBlock)) &&
                                !(agentPos !=null && point.equals(agentPos.getLimited()) && cellObject.getObjectType() == FBObjectType.__FBAgent)) {
                            map.addCellObject(point, cellObject, cellMap.get(point).getLatestStepMark());
                        }
                        if(cellObject.getObjectType().equals(FBObjectType.__FBTaskBoard)){
                            map.addTaskBoard(point);
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            HorseRider.yell(TAG, "getMapSnapshot: null in map " + getName() +
                    " point:" + debugPoint , e);
            HorseRider.yell(TAG, "getMapSnapshot: null in map " + getName() +
                    " content:" + cellMap.get(debugPoint) );
            HorseRider.yell(TAG, "getMapSnapshot: null in map " + getName() +
                    " mark:" + cellMap.get(debugPoint).getLatestStepMark() );
            HorseRider.yell(TAG, "getMapSnapshot: null in map " + getName() +
                    " agent pos:" + agentPos);
            /*HorseRider.yell(TAG, "getMapSnapshot: null in map " + getName() +
                    " point:" + debugPoint +
                    " content:" + cellMap.get(debugPoint) +
                    " mark:" + cellMap.get(debugPoint).getLatestStepMark() +
                    " agent pos:" + agentPos);*/
            /*HorseRider.yell(TAG, "getMapSnapshot: null in map " + getName(), e);
            printMap();
            for (Point point : cellMap.keySet()) {
                HorseRider.yell(TAG, "getMapSnapshot: null in map " + getName() +
                        " point:" + point +
                        " content:" + cellMap.get(point) +
                        " mark:" + cellMap.get(point).getLatestStepMark() +
                        " agent pos:" + agentPos);
                if (cellMap.get(point).getLatestStepMark() >= stepLimit) {
                    map.insertEmptyVisionCells(0, point, cellMap.get(point).getLatestStepMark());
                    for (FBCellObject cellObject : this.cellMap.get(point).getLatestCellContent().values()) {
                        HorseRider.yell(TAG, "getMapSnapshot: null in map " + getName() +
                                " cell object:" + cellObject);
                        if (!(bodyList != null && bodyList.contains(point) && cellObject.getObjectType().equals(FBObjectType.__FBBlock)) &&
                                !(agentPos !=null && point.equals(agentPos.getLimited()) && cellObject.getObjectType() == FBObjectType.__FBAgent)) {
                            map.addCellObject(point, cellObject, cellMap.get(point).getLatestStepMark());
                        }
                    }
                }
            }*/
            throw new ShouldNeverHappen(TAG + " getMapSnapshot: " + this.getName() +
                    "null in map");
        }
        return map;
    }

    @Override
    public String toString() {
        FBCell node;
        StringBuilder textMap = new StringBuilder();
        int borderX = 0;
        int borderY = 0;
        int fromY;
        int fromX;
        int toY;
        int toX;
        if (Point.LoopLimit.isFoundX()) {
            borderX = -Point.LoopLimit.getLoopX() / 2 - 1;
            fromX = borderX;
            toX = -borderX;
            //fromX = Math.max(borderX, this.getXMin());
            //toX = Math.min(-borderX, this.getXMax());
        } else {
            fromX = this.getXMin();
            toX = this.getXMax();
        }
        if (Point.LoopLimit.isFoundY()) {
            borderY = -Point.LoopLimit.getLoopY() / 2 - 1;
            fromY = borderY;
            toY = -borderY;
            //fromY = Math.max(borderY, this.getYMin());
            //toY = Math.min(-borderY, this.getYMax());
        } else {
            fromY = this.getYMin();
            toY = this.getYMax();
        }

        // for (int j = this.getYMin(); j <= this.getYMax(); j++) {
        //    for (int i = this.getXMin(); i <= this.getXMax(); i++) {
        for (int y = fromY; y <= toY; y++) {
            for (int x = fromX; x <= toX; x++) {
                int lastTextLength = textMap.length();
                if (Point.LoopLimit.isFoundY() && (y == borderY || y == -borderY)) {
                    textMap.append("-");
                } else if (Point.LoopLimit.isFoundX() && (x == borderX || x == -borderX)) {
                    textMap.append("|");
                } else {
                    node = this.getNode(new Point(x, y));
                    if ((y == 0) && (x == 0)) {
                        textMap.append("X");
                    }
                    if (node != null) {
                        if (node.containsType(FBObjectType.__FBClear))
                            textMap.append(".");
                        if (node.containsType(FBObjectType.__FBObstacle))
                            textMap.append("O");
                        if (node.containsType(FBObjectType.__FBAgent)) {
                            String name = ((FBEntityObject) node.getFirstByType(FBObjectType.__FBAgent)).getName();
                            textMap.append(name, name.length() - 1, name.length()); // append last char
                            //textMap.append("A");
                        }
                        if (node.containsType(FBObjectType.__FBEntity_Friend))
                            textMap.append("F");
                        if (node.containsType(FBObjectType.__FBEntity_Enemy))
                            textMap.append("E");
                        if (node.containsType(FBObjectType.__FBMarker))
                            textMap.append("M");
                        if (node.containsType(FBObjectType.__FBGoal))
                            textMap.append("G");
                        if (node.containsType(FBObjectType.__FBBlock))
                            textMap.append(node.getBlockObject().getBlockType().getName());
                        if (node.containsType(FBObjectType.__FBDispenser)) {
                            textMap.append("D");
                        }
                        if (node.containsType(FBObjectType.__FBTaskBoard)) {
                            textMap.append("T");
                        }

                    } else {  // no cell
                        textMap.append(" ");
                    }
                }
                if (textMap.length() - lastTextLength >= MAP_SPACER_SIZE) {
                    textMap.delete(lastTextLength + MAP_SPACER_SIZE, textMap.length());
                } else {
                    if (textMap.length() == lastTextLength) { // nothing new, add "."
                        textMap.append(".");
                    }
                    textMap.append(" ".repeat(MAP_SPACER_SIZE - Math.max(0, textMap.length() % MAP_SPACER_SIZE))); // add up to MAP_SPACER_SIZE spaces
                }

            }
            textMap.append(" ".repeat(MAP_SPACER_SIZE - 1)).append("\n");
        }
        StringBuilder limitString = new StringBuilder();
        if (Point.LoopLimit.isFoundX() || Point.LoopLimit.isFoundY()) {
            limitString.append(" Limit:");
            if (Point.LoopLimit.isFoundX()) {
                limitString.append(" X: ").append(Point.LoopLimit.getLoopX());
            }
            if (Point.LoopLimit.isFoundY()) {
                limitString.append(" Y: ").append(Point.LoopLimit.getLoopY());
            }
        } else {
            limitString.append(" No limits yet");
        }
        return this.getName() +
                " Map min: [" + this.getXMin() + "," + this.getYMin() +
                "] max: [" + this.getXMax() + "," + this.getYMax() + "]" +
                limitString + "\n" +
                textMap;

    }

    public void printMap() {
        HorseRider.inquire(TAG, "printMap: " + this);
    }

    Set<Point> getCellList() {
        return cellMap.keySet();
    }

    /**
     * returns block object if exists otherwise null
     *
     * @param pAt point of retrieval
     * @return block object or null
     */
    public FBBlockObject getBlockObjectAt(Point pAt) {
        FBCell fbCell = cellMap.get(pAt.getLimited());
        if (fbCell != null) {
            return fbCell.getBlockObject();
        }
        return null;
    }


    /**
     * returns dispenser object if exists otherwise null
     *
     * @param at point of retrieval
     * @return block object or null
     */
    public FBDispenserObject getDispenserObjectAt(Point at) {
        FBCell fbCell = getNode(at);
        if (fbCell != null) {
            FBCellObject dispenser = fbCell.getFirstByType(FBObjectType.__FBDispenser);
            if (dispenser instanceof FBDispenserObject) {
                return (FBDispenserObject) dispenser;
            }
        }
        return null;
    }

    public HashSet<Point> getSeeingAgents(Point agentPos, int radius) {
        HashSet<Point> list = new HashSet<>();
        for (int j = -radius; j <= radius; j++) {
            for (int i = -radius; i <= radius; i++) {
                if (Math.abs(i) + Math.abs(j) <= radius) { // only in vision range
                    Point point = new Point(agentPos.x + i, agentPos.y + j);
                    Point limitedPoint = point.getLimited();
                    if (cellMap.containsKey(limitedPoint) && (cellMap.get(limitedPoint).containsType(FBObjectType.__FBEntity_Friend))) {
                        list.add(new Point(i, j));
                    }
                }
            }
        }
        return list;
    }

    public void insertEmptyVisionCells(int radius, Point agentPos, int step) {
        updateStep(step);
        for (int j = -radius; j <= radius; j++) {
            for (int i = -radius; i <= radius; i++) {
                if (Math.abs(i) + Math.abs(j) <= radius) { // only in vision range
                    Point point = new Point(agentPos.x + i, agentPos.y + j).getLimited();
                    if (!cellMap.containsKey(point)) {
                        addEmptyCell(point);
                    }
                    cellMap.get(point).insertLevel(step);
                }
            }
        }
    }

    private FBCell addEmptyCell(Point pPoint) {
        Point point = pPoint.getLimited();
        FBCell cell = null;
        if (!cellMap.containsKey(point)) {
            cell = new FBCell(getName());
            this.addCell(point, cell);
        } else {
            HorseRider.yell(TAG, "addEmptyCell: trying to override existing cell at " + point.toString());
        }
        return cell;
    }

    public boolean addCellObject(Point pPlace, FBCellObject object, int step) {
        Point place = pPlace.getLimited();
        updateStep(step);
        FBCell cell = cellMap.get(place);
        if (cell == null) {
            cell = this.addEmptyCell(place);
        }
        return cell.insertObject(object, step) != null;
    }

    private void addCell(Point pPoint, FBCell cell) {
        Point point = pPoint.getLimited();
        cellMap.put(point, cell);
        updateXYMinMax(point);
    }

    private void updateXYMinMax(Point cellPosition) {
        PXMax = Math.max(PXMax, cellPosition.x);
        PXMin = Math.min(PXMin, cellPosition.x);
        PYMax = Math.max(PYMax, cellPosition.y);
        PYMin = Math.min(PYMin, cellPosition.y);
    }


    public boolean isTraversableAt(Point at, Set<Point> list, Rotation rotation) {
        return isTraversableAt(at, list, rotation, Collections.emptySet());
    }

    public boolean isTraversableAt(Point at, Set<Point> list, Rotation rotation, Set<Point> bodyCollisionIgnoreList) {
        for (Point point : list) {
            Point rotated = new Point(point).rotate(rotation);
            if (!bodyCollisionIgnoreList.contains(at.sum(rotated)) && !isTraversableAt(at.sum(rotated))) {
                return false;
            }
        }
        return bodyCollisionIgnoreList.contains(at) || isTraversableAt(at);
    }

    private void updateStep(int step) {
        this.step = Math.max(this.step, step);
    }

    int latestStep() {
        return this.step;
    }


    /*@Deprecated
    Border getBorder() {
        return border;
    }

    @Deprecated
    void importBorder(Border border, Point memberPoint) {
        this.border.importBorder(border, memberPoint);
    }


    @Deprecated
    public void setBorder(Point mapPosition, Direction direction) {
        border.addBorder(mapPosition, direction);
    }
    */
    boolean getNodeContainsType(Point sum, FBObjectType fbEntity_friend) {
        if (getNode(sum) == null) return false;
        return getNode(sum).containsType(fbEntity_friend);
    }

    public FBCellObject getNodeFirstByType(Point position, FBObjectType fbObjectType) {
        if (getNode(position) == null) return null;
        return getNode(position).getFirstByType(fbObjectType);
    }
    public String getCellSource(Point position) {
        if (getNode(position) == null) return null;
        return getNode(position).getOwner();
    }


    /*@Deprecated
    public static class Border { // this expects the border to always be box !!!!
        private boolean westIsSet = false;
        private boolean eastIsSet = false;
        private boolean northIsSet = false;
        private boolean southIsSet = false;
        private int westBorder;
        private int eastBorder;
        private int northBorder;
        private int southBorder;

        @Deprecated
        public boolean isOutside(Point at) {
            if (westIsSet && at.x <= westBorder) {
                return true;
            }
            if (eastIsSet && at.x >= eastBorder) {
                return true;
            }
            if (northIsSet && at.y <= northBorder) {
                return true;
            }
            //noinspection RedundantIfStatement
            if (southIsSet && at.y >= southBorder) {
                return true;
            }
            return false;
        }

        @Deprecated
        public void addBorder(Point agentPos, Direction direction) {
            switch (direction) {
                case W:
                    westIsSet = true;
                    westBorder = agentPos.x - 1;
                    break;
                case E:
                    eastIsSet = true;
                    eastBorder = agentPos.x + 1;
                    break;
                case N:
                    northIsSet = true;
                    northBorder = agentPos.y - 1;
                    break;
                case S:
                    southIsSet = true;
                    southBorder = agentPos.y + 1;
                    break;
                case __UNKNOWN:
                    break;
            }
        }

        @Deprecated
        void importBorder(Border border, Point displacement) {
            if (!this.northIsSet && border.northIsSet) {
                northBorder = border.northBorder + displacement.y;
                northIsSet = true;
            }
            if (!this.westIsSet && border.westIsSet) {
                westBorder = border.westBorder + displacement.x;
                westIsSet = true;
            }
            if (!this.southIsSet && border.southIsSet) {
                southBorder = border.southBorder + displacement.y;
                southIsSet = true;
            }
            if (!this.eastIsSet && border.eastIsSet) {
                eastBorder = border.eastBorder + displacement.x;
                eastIsSet = true;
            }
        }
    }*/
}

