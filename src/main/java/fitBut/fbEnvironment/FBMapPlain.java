

/*

 * Mapa je m��ka s neur�it�mi rozm�ry. Je implementov�na jako seznam pozorovan�ch pozic a jejich naposledy zji�t�n�ch stav�.
 * Mapa je p�i�azena jednomu nebo v�ce agent�m. Na po��tku m� ka�d� agent svoji mapu a koordin�ty jsou po��t�ny od po��te�n� pozice
 * ka�d�ho agenta. V p��pad� �e se agenti potkaj�, slou�� sv� mapy, koordin�ty jsou (*n�jak*) p�epo��t�ny a agenti tuto mapu pak sd�l�.
 * Do mapy si agent zna�� pozorov�n� s �asov�m raz�tkem po��zen� pozorov�n�. Aktu�ln� jsou takov� stavy jednotliv�ch pozic v map�

 */

package fitBut.fbEnvironment;

import fitBut.fbEnvironment.FBCells.objects.FBDispenserObject;
import fitBut.fbEnvironment.utils.Rotation;
import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.FBCells.FBCell;
import fitBut.fbEnvironment.FBCells.objects.FBCellObject;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbPerceptionModule.data.BlockType;

import fitBut.utils.Point;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FBMapPlain extends FBMap {

    @SuppressWarnings("unused")
    private static final String TAG = "FBMapPlain";

    private HashSet<Point> taskBoards = new HashSet<>();

    /**
     * checks if cell exists
     *
     * @param pPoint position in question
     * @return true cell if exists
     */
    @Override
    public boolean hasCellAt(Point pPoint) {
        Point position = pPoint.getLimited();
        return cellMap.containsKey(position);
    }

    /*@Override
    public boolean isOutside(Point point) {
        return border.isOutside(point);
    }*/

    @Override
    FBCell getNode(Point pPoint) {
        return cellMap.get(pPoint.getLimited());
    }

    @Override
    void recalculateForNewLimits() {
        migrateCellMap();
    }


    public int getXMin() {
        return (PXMin);
    }

    public int getXMax() {
        return (PXMax);
    }

    public int getYMin() {
        return (PYMin);
    }

    public int getYMax() {
        return (PYMax);
    }

    public FBMapPlain(String name) {
        super(name);
    }

    @Deprecated
    public LinkedList<BlockType> getBlockTypes() {
        LinkedList<BlockType> blockTypes = new LinkedList<>();
        for (FBCell cellWithType : cellMap.values()) {                    // for all cells with specified type
            FBCellObject cellObject = cellWithType.getLatestCellContent().get(FBObjectType.__FBBlock);
            if (cellObject != null) {
                BlockType blockType = ((FBBlockObject) cellObject).getBlockType();
                if (!blockTypes.contains(blockType)) {
                    blockTypes.add(blockType);
                }
            }
            cellObject = cellWithType.getLatestCellContent().get(FBObjectType.__FBDispenser);
            if (cellObject != null) {
                BlockType blockType = ((FBDispenserObject) cellObject).getDispenserType();
                if (!blockTypes.contains(blockType)) {
                    blockTypes.add(blockType);
                }
            }
        }
        return blockTypes;
    }

    @Override
    public boolean isTraversableAt(Point pAt) {
        Point at = pAt.getLimited();
        if (!cellMap.containsKey(at)) { //border.isOutside(at) ||
            return false;
        }
        return cellMap.get(at).isTraversable();
    }

    /**
     * Creates snapshot of map - dumping history a flattening all to last step
     *
     * @param stepLimit step limit
     * @return map snapshot
     */
    @Override
    public FBMapPlain getMapSnapshot(int stepLimit) {
        return getMapSnapshot(stepLimit, null, null);
    }


    public boolean collidesWithUnknown(Point at, Set<Point> list, Rotation rotation) {
        for (Point point : list) {
            Point rotated = new Point(point).rotate(rotation);
            if (!hasCellAt(at.sum(rotated))) {
                return true;
            }
        }
        return !hasCellAt(at);
    }

    public void importLayer(FBMapLayer mergeLayer) {
        for (Point point : mergeLayer.getLayerPoints()) {
            this.insertNode(point, mergeLayer.getNode(point));
        }
    }

    /**
     * imports cell content
     * ignores if same type present
     *
     * @param point where to import
     * @param node  node containing types
     */
    private void insertNode(Point point, FBCell node) {
        if (node.getLatestCellContent().isEmpty()) {
            this.insertEmptyVisionCells(0, point, latestStep());
        } else {
            for (FBCellObject cellObject : node.getLatestCellContent().values()) {
                this.addCellObject(point, cellObject, latestStep());
            }
        }
    }

    public boolean doesNotHaveGoal() { //todo: cache
        for (FBCell cellWithType : cellMap.values()) {                    // for all cells with specified type
            FBCellObject cellObject = cellWithType.getLatestCellContent().get(FBObjectType.__FBGoal);
            if (cellObject != null) {
                return false;
            }
        }
        return true;
    }

    public boolean doesNotHaveTakBoard() { // WARNING: only work on snapshots TODO: expand
        /*
        for (FBCell cellWithType : cellMap.values()) {                    // for all cells with specified type
            FBCellObject cellObject = cellWithType.getLatestCellContent().get(FBObjectType.__FBTaskBoard);
            if (cellObject != null) {
                return false;
            }
        }
        return true;
        */
        return taskBoards.size()==0;
    }

    public boolean hasBlockOrDispenser() { //todo: cache
        for (FBCell cellWithType : cellMap.values()) {                    // for all cells with specified type
            ConcurrentHashMap<FBObjectType, FBCellObject> latestCellContent = cellWithType.getLatestCellContent();

            if (latestCellContent.get(FBObjectType.__FBBlock) != null || latestCellContent.get(FBObjectType.__FBDispenser) != null) {
                return true;
            }
        }
        return false;
    }

    public boolean doesNotHaveClearEventsIn(Set<Point> list) {
        for (Point point : list) {
            FBCell cellWithType = cellMap.get(point);
            if (cellWithType == null) continue;
            FBCellObject cellObject = cellWithType.getLatestCellContent().get(FBObjectType.__FBMarker);
            if (cellObject != null) {
                return false;
            }
        }
        return true;
    }

    public void addTaskBoard(Point point) {
        taskBoards.add(point);
    }

    public Set<Point> getTaskBoards() {
        return taskBoards;
    }
}

