package fitBut.utils.exceptions;

import fitBut.utils.logging.HorseRider;

/**
 * @author : Vaclav Uhlir
 * @since : 07/10/2019
 **/

public class ShouldNeverHappen extends RuntimeException {
    private static final String TAG = "ShouldNeverHappen";

    public ShouldNeverHappen(String string) {
        super(string);
        HorseRider.yell(TAG, " " + string, this);
    }
}

