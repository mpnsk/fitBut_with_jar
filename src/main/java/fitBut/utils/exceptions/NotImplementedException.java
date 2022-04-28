package fitBut.utils.exceptions;

import fitBut.utils.logging.HorseRider;

/**
 * @author : Vaclav Uhlir
 * @since : 07/10/2019
 **/

public class NotImplementedException extends RuntimeException {
    private static final String TAG = "NotImplementedException";

    public NotImplementedException(String string) {
        super(string);
        HorseRider.yell(TAG, " " + string, this);
    }
}

