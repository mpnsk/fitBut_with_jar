package fitBut.fbEnvironment.FBCells.objects;

import fitBut.fbEnvironment.utils.FBObjectType;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public class FBOtherObject extends FBCellObject {

    public FBOtherObject(FBObjectType objectType) {
        super(objectType);
    }

    @Override
    public boolean isSame(FBCellObject cellObject) {
        return cellObject.getObjectType() == this.getObjectType();
    }

    @Override
    public FBCellObject getClone() {
        return new FBOtherObject(this.getObjectType());
    }
}
