package fitBut.fbEnvironment.FBCells.objects;

import fitBut.fbEnvironment.utils.FBObjectType;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public abstract class FBCellObject {
    private static final String TAG = "FBCellObject";
    private final FBObjectType objectType;

    FBCellObject(FBObjectType objectType) {
        this.objectType = objectType;
    }

    public FBObjectType getObjectType() {
        return objectType;
    }

    public abstract boolean isSame(FBCellObject cellObject);

    public abstract FBCellObject getClone();

    @Override
    public String toString() {
        return TAG + " - " + objectType.toString();
    }
}
