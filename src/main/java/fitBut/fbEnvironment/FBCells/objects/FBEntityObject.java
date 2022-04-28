package fitBut.fbEnvironment.FBCells.objects;

import fitBut.fbEnvironment.utils.FBObjectType;

/**
 * @author : Vaclav Uhlir
 * @since : 13.9.2019
 **/
public class FBEntityObject extends FBCellObject {
    private String team;
    private String name;

    public FBEntityObject(FBObjectType type, String team) {
        super(type);
        this.team = team;
    }

    public FBEntityObject(FBObjectType type, String team, String name) {
        super(type);
        this.team = team;
        this.name = name;
    }

    private String getTeam() {
        return team;
    }

    @Override
    public boolean isSame(FBCellObject cellObject) {
        return cellObject.getObjectType() == this.getObjectType() && ((FBEntityObject) cellObject).getTeam().equals(team);
    }

    @Override
    public FBCellObject getClone() {
        return new FBEntityObject(getObjectType(), getTeam())
                .setName(this.getName());
    }

    public String getName() {
        return name;
    }

    public FBEntityObject setName(String name) {
        this.name = name;
        return this;
    }
}
