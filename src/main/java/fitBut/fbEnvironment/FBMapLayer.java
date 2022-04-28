package fitBut.fbEnvironment;

import fitBut.fbEnvironment.FBCells.FBCell;
import fitBut.fbEnvironment.FBCells.objects.FBCellObject;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.utils.Point;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Vaclav Uhlir
 * @since : 25.9.2019
 **/
public class FBMapLayer extends FBMap {

    @SuppressWarnings("unused")
    private static final String TAG = "FBMapLayer";
    private final FBMap ancestor;

    public FBMapLayer(String name, FBMap ancestor) {
        super(name);
        this.ancestor = ancestor;
        PXMax = ancestor.getXMax();
        PXMin = ancestor.getXMin();
        PYMax = ancestor.getYMax();
        PYMin = ancestor.getYMin();
    }

    @Override
    FBCell getNode(Point pPoint) {
        Point point = pPoint.getLimited();
        if (cellMap.get(point) != null) {
            return cellMap.get(point);
        } else {
            return ancestor.getNode(point);
        }
    }

    @Override
    void recalculateForNewLimits() {
        migrateCellMap();
        ancestor.checkLimitChange();
    }

    @Override
    public boolean hasCellAt(Point pPoint) {
        Point position = pPoint.getLimited();
        return cellMap.containsKey(position) || ancestor.hasCellAt(position);
    }

    @Override
    public int getXMin() {
        return PXMin;
    }

    @Override
    public int getXMax() {
        return PXMax;
    }

    @Override
    public int getYMin() {
        return PYMin;
    }

    @Override
    public int getYMax() {
        return PYMax;
    }

    @Override
    public boolean isTraversableAt(Point at) {
        /*if (this.isOutside(at)) {
            return false;
        }*/
        FBCell node = getNode(at);
        if (node != null) {
            return node.isTraversable();
        }
        return false; // nonexistent node is not traversable
    }

    @Override
    public FBMapPlain getMapSnapshot(int stepLimit) {
        FBMapPlain map = new FBMapPlain(this.getName() + " snapshot");
        FBMap source = this;
        while (source != null) {                 //for history
            for (Point point : source.getCellList()) {                  //for every cell
                if (!map.hasCellAt(point)) {
                    map.insertEmptyVisionCells(0, point, source.getNode(point).getLatestStepMark());// add cell
                    ConcurrentHashMap<FBObjectType, FBCellObject> cellContent = getNode(point).getLatestCellContent();
                    for (FBCellObject cellObject : cellContent.values()) {
                        map.addCellObject(point, cellObject, source.getNode(point).getLatestStepMark());
                    }
                }
            }
            if (source instanceof FBMapLayer) {
                source = ((FBMapLayer) source).getAncestor();
            } else if (source instanceof FBMapPlain) {
                //map.importBorder(source.getBorder(), new Point(0, 0));
                source = null;
            }
        }
        return map;
    }

    private FBMap getAncestor() {
        return ancestor;
    }

}
