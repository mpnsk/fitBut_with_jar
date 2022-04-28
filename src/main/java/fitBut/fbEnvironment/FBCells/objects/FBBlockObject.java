package fitBut.fbEnvironment.FBCells.objects;

import fitBut.agents.FBAgent;
import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbPerceptionModule.data.BlockType;

import java.util.HashSet;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public class FBBlockObject extends FBCellObject {
    private BlockType blockType;
    private boolean attached = false;
    private final HashSet<FBAgent> attachedTo = new HashSet<>();

    public FBBlockObject() {
        super(FBObjectType.__FBBlock);
    }

    public FBBlockObject(BlockType blockType, boolean attached) {
        super(FBObjectType.__FBBlock);
        this.blockType = blockType;
        this.attached = attached;
    }

    public BlockType getBlockType() {
        return blockType;
    }

    @Override
    public boolean isSame(FBCellObject cellObject) {
        return cellObject.getObjectType() == FBObjectType.__FBBlock &&
                ((FBBlockObject) cellObject).getBlockType().equals(blockType) &&
                ((FBBlockObject) cellObject).getAttachedTo().equals(getAttachedTo());
    }

    private HashSet<FBAgent> getAttachedTo() {
        return attachedTo;
    }

    @Override
    public FBCellObject getClone() {
        return new FBBlockObject(blockType, attached).setAttachedToCopy(this.getAttachedTo());
    }

    private FBBlockObject setAttachedToCopy(HashSet<FBAgent> attachedTo) {
        this.attachedTo.addAll(attachedTo);
        return this;
    }

    private void addAttachedTo(HashSet<FBAgent> attachedTo, int latest) {
        this.attachedTo.addAll(attachedTo);
        this.attached = attached || (!this.attachedTo.isEmpty());
        if (attachedTo.size() > 1) {
            for (FBAgent agent : this.attachedTo) {
                agent.setMultipleAgentsOnBlock(latest);
            }
        }
    }

    public boolean notAttached() {
        return !attached;
    }

    public FBBlockObject addAttachedTo(FBAgent attachedTo) {
        this.attached = this.attached || (attachedTo != null);
        this.attachedTo.add(attachedTo);
        return this;
    }

    public boolean isNotAttachedTo(FBAgent partner) {
        return !attachedTo.contains(partner);
    }

    public void mergeAttached(FBBlockObject newBlock, int latest) {
        this.addAttachedTo(newBlock.getAttachedTo(), latest);
    }
}
