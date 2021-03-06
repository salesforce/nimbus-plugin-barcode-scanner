package com.salesforce.barcodescannerplugin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * the error code the barcode scan could possible through
 */
@Serializable
enum class BarcodeScannerFailureCode {

    /**
     * the user clicked the button to dismiss the scanner
     */
    @SerialName("userDismissedScanner")
    USER_DISMISSED_SCANNER,

    /**
     * ios: permission was disabled by the user and will need to be turned on in settings
     * android: permission was denied by user when prompt, could ask again
     */
    @SerialName("userDeniedPermission")
    USER_DENIED_PERMISSION,

    /**
     * android: permission was denied along "don't ask again" when prompt, will need to go app setting to turn on
     */
    @SerialName("userDisabledPermissions")
    USER_DISABLED_PERMISSION,

    /**
     * A hardware or unknown failure happened when trying to use the camera
     * or other reasion, like FirebaseVision failure.
     * This is not caused by a lack of permission.
     */
    @SerialName("unknownReason")
    UNKNOWN_REASON,

    /**
     * android only: the hosting activity could be destroyed while scanning is in
     * foreground, as a result the success or failure can't delivered to webview.
     * It could be delivered to hosting activity when recreated after
     * leaving the scanning activity, but not the webview
     */
    @SerialName("bridgeUnavailable")
    BRIDGE_UNAVAILABLE;
}
