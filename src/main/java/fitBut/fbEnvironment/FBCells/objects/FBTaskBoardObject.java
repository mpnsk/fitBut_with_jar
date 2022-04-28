package fitBut.fbEnvironment.FBCells.objects;

import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbPerceptionModule.data.BlockType;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public class FBTaskBoardObject extends FBCellObject {
    private BlockType taskboardType;

    public FBTaskBoardObject(BlockType taskboardType) {
        super(FBObjectType.__FBTaskBoard);
        this.taskboardType = taskboardType;
    }

    public BlockType getTaskBoardType() {
        return taskboardType;
    }

    @Override
    public boolean isSame(FBCellObject cellObject) {
        return cellObject.getObjectType() == FBObjectType.__FBTaskBoard && ((FBTaskBoardObject) cellObject).getTaskBoardType().equals(taskboardType);
    }

    @Override
    public FBCellObject getClone() {
        return new FBTaskBoardObject(taskboardType);
    }
}
