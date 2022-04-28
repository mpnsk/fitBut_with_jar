package fitBut.fbEnvironment.FBCells.objects;

import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbPerceptionModule.data.BlockType;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public class FBDispenserObject extends FBCellObject {
    private BlockType dispenserType;

    public FBDispenserObject(BlockType dispenserType) {
        super(FBObjectType.__FBDispenser);
        this.dispenserType = dispenserType;
    }

    public BlockType getDispenserType() {
        return dispenserType;
    }

    @Override
    public boolean isSame(FBCellObject cellObject) {
        return cellObject.getObjectType() == FBObjectType.__FBDispenser && ((FBDispenserObject) cellObject).getDispenserType().equals(dispenserType);
    }

    @Override
    public FBCellObject getClone() {
        return new FBDispenserObject(dispenserType);
    }
}
