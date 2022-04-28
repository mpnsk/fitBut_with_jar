package fitBut.fbEnvironment.utils;

import fitBut.utils.logging.HorseRider;

import java.util.ArrayList;

/**
 * @author : Vaclav Uhlir
 * @since : 14.9.2019
 **/
public enum Rotation {
    CW("cw"),
    CCW("ccw"),
    NULL("null"),
    OPPOSITE("opposite"),
    __UNKNOWN("");

    private String text;
    private static final String TAG = "Rotation";

    Rotation(String text) {
        this.text = text;
    }

    public static ArrayList<Rotation> fourDirections() {
        ArrayList<Rotation> list = new ArrayList<>();
        list.add(NULL);
        list.add(CW);
        list.add(CCW);
        list.add(OPPOSITE);
        return list;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return getText();
    }

    @SuppressWarnings("unused")
    public static Rotation fromString(String text) {
        for (Rotation rotation : Rotation.values()) {
            if (rotation.text.equalsIgnoreCase(text)) {
                return rotation;
            }
        }
        HorseRider.yell(TAG, "fromString: unknown rotation! " + text);
        return __UNKNOWN;
    }

    public Rotation mirrored() {
        switch (this) {
            case CW:
                return Rotation.CCW;
            case CCW:
                return Rotation.CW;
            case NULL:
            case OPPOSITE:
            case __UNKNOWN:
        }
        return this;
    }

    public Direction clockDirection() {
        switch (this) {
            case CW:
                return Direction.E;
            case CCW:
                return Direction.W;
            case OPPOSITE:
                return Direction.S;
            case NULL:
                return Direction.N;
        }
        return Direction.__UNKNOWN;
    }
}
