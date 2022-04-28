package fitBut.fbPerceptionModule.data;

/**
 * @author : Vaclav Uhlir
 * @since : 13.9.2019
 **/
public class BlockType {
    private final String blockName;

    public BlockType(String blockName) {
        this.blockName = blockName;
    }

    public String getName() {
        return blockName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockType) {
            return ((BlockType) obj).getName().equals(getName());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "[" + blockName + "]";
    }
}
