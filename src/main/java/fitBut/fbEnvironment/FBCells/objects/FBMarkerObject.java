package fitBut.fbEnvironment.FBCells.objects;

import fitBut.fbEnvironment.utils.FBObjectType;
import fitBut.fbEnvironment.utils.MarkerType;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public class FBMarkerObject extends FBCellObject {

    private MarkerType markerType;

    private FBMarkerObject(MarkerType markerType) {
        super(FBObjectType.__FBMarker);
        this.markerType = markerType;
    }

    public FBMarkerObject(String markerString) {
        super(FBObjectType.__FBMarker);
        this.markerType = MarkerType.fromString(markerString);
    }

    @Override
    public boolean isSame(FBCellObject cellObject) {
        return cellObject.getObjectType() == FBObjectType.__FBMarker &&
                (((FBMarkerObject) cellObject).getMarkerType() == markerType);
    }

    @Override
    public FBCellObject getClone() {
        return new FBMarkerObject(markerType);
    }

    public MarkerType getMarkerType() {
        return markerType;
    }
}
