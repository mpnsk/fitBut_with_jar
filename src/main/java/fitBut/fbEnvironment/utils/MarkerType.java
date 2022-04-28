package fitBut.fbEnvironment.utils;

import fitBut.utils.logging.HorseRider;


/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public enum MarkerType {
    CLEAR("clear"),
    CP("cp"),
    CI("ci"),
    REQUEST("request"),
    __UNKNOWN("");

    private String text;
    private static final String TAG = "MarkerType";

    MarkerType(String text) {
        this.text = text;
    }

    public static MarkerType fromString(String text) {
        for (MarkerType markerType : MarkerType.values()) {
            if (markerType.text.equalsIgnoreCase(text)) {
                return markerType;
            }
        }
        HorseRider.yell(TAG, "fromString: unknown marker! " + text);
        return __UNKNOWN;
    }

}
