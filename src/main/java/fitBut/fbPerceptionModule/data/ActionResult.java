package fitBut.fbPerceptionModule.data;

import fitBut.utils.logging.HorseRider;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public enum ActionResult {
    SUCCESS("success"),
    FAILED_RANDOM("failed_random"),
    FAILED("failed"),
    FAILED_PATH("failed_path"),
    FAILED_FORBIDDEN("failed_forbidden"),
    FAILED_BLOCKED("failed_blocked"),
    FAILED_PARAMETER("failed_parameter"),
    FAILED_TARGET("failed_target"),
    FAILED_STATUS("failed_status"),
    FAILED_PARTNER("failed_partner"),
    FAILED_RESOURCES("failed_resources"),
    FAILED_LOCATION("failed_location"),
    TMP_OP_REVERSE("TMP_OP_REVERSE"),
    __NONE("");

    private String text;
    private static final String TAG = "ActionResult";

    ActionResult(String text) {
        this.text = text;
    }

    public static ActionResult fromString(String text) {
        for (ActionResult actionResult : ActionResult.values()) {
            if (actionResult.text.equalsIgnoreCase(text)) {
                if (actionResult == FAILED_PARAMETER) {
                    HorseRider.yell(TAG, "fromString: FAILED_PARAMETER");
                }
                return actionResult;
            }
        }
        HorseRider.yell(TAG, "fromString: unknown result! " + text);
        return __NONE;
    }

}

