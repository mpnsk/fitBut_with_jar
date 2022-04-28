package fitBut.fbEnvironment.FBCells;

import fitBut.fbEnvironment.FBCells.objects.FBBlockObject;
import fitBut.fbEnvironment.FBCells.objects.FBCellObject;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.utils.logging.HorseRider;

import java.util.concurrent.ConcurrentHashMap;

public class FBCell {
    @SuppressWarnings("unused")
    private static final String TAG = "FBCell";
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<FBObjectType, FBCellObject>> cellContent;
    private int latest = -1;
    private final String owner;

    public FBCell(String name) {
        owner = name;
        cellContent = new ConcurrentHashMap<>();
    }

    public boolean containsType(FBObjectType type) {
        return getLatestCellContent().containsKey(type);
    }

    public ConcurrentHashMap<FBObjectType, FBCellObject> getLatestCellContent() {
        return cellContent.getOrDefault(latest, new ConcurrentHashMap<>()); // latest or this is an empty cell
    }

    @Deprecated
    public boolean containsObject(FBCellObject cellObject) {
        for (FBCellObject object : getLatestCellContent().values()) {    // for al local content
            if (object.isSame(cellObject)) {
                return true;
            }
        }
        return false;
    }

    /**
     * insert object
     *
     * @param object to be inserted
     * @param step   step
     * @return null if object collides
     */
    public FBCell insertObject(FBCellObject object, int step) {
        insertLevel(step);
        if (cellContent.get(step).get(object.getObjectType()) != null) {
            return null;
        }
        cellContent.get(step).put(object.getObjectType(), object);
        return this;
    }

    public boolean isTraversable() { //TODO optimize
        return !containsType(FBObjectType.__FBObstacle) &&
                !containsType(FBObjectType.__FBAgent) &&
                !containsType(FBObjectType.__FBEntity_Enemy) &&
                !containsType(FBObjectType.__FBEntity_Friend) &&
                !containsType(FBObjectType.__FBBlock);
    }

    public FBCellObject getFirstByType(FBObjectType type) {
        return getLatestCellContent().get(type);
    }

    @Deprecated
    public void removeObjectByType(FBObjectType type) {
        for (int i = latest; i >= 0; i--) {    // for all history
            if (cellContent.containsKey(i)) {
                cellContent.get(i).remove(type);
            }
        }
    }

    public void insertLevel(int step) {
        cellContent.putIfAbsent(step, new ConcurrentHashMap<>());
        latest = Math.max(latest, step);
    }

    public int getLatestStepMark() {
        return latest;
    }

    public boolean isNotSame(FBCell node) {
        return !getLatestCellContent().equals(node.getLatestCellContent());
    }

    public FBBlockObject getBlockObject() {
        FBCellObject block = this.getFirstByType(FBObjectType.__FBBlock);
        if (block instanceof FBBlockObject) {
            return (FBBlockObject) block;
        }
        return null;
    }

    public void merge(FBCell fbCell) {
        //private ConcurrentHashMap<Integer, ConcurrentHashMap<FBObjectType, FBCellObject>> cellContent;
        ConcurrentHashMap<Integer, ConcurrentHashMap<FBObjectType, FBCellObject>> importSet = fbCell.getCellContent();
        for (int level: importSet.keySet()){ // for all import levels
            insertLevel(level);
            ConcurrentHashMap<FBObjectType, FBCellObject> levelSet = importSet.get(level);
            for (FBCellObject cellObject :levelSet.values()) {  // for all content
                if(insertObject(cellObject,level)==null){
                    HorseRider.warn(TAG, "merge: conflict at: "+level+" "+cellObject);
                }
            }
        }
    }

    private ConcurrentHashMap<Integer, ConcurrentHashMap<FBObjectType, FBCellObject>> getCellContent() {
        return cellContent;
    }

    public String getOwner() {
        return owner;
    }
}
