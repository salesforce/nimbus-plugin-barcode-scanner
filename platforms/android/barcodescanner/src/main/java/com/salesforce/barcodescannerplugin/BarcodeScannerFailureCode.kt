package com.salesforce.barcodescannerplugin

import com.salesforce.nimbus.JSONSerializable

/**
 * the error code the barcode scan could possible through
 */
enum class BarcodeScannerFailureCode(val value: String) : JSONSerializable {

    /**
     * the user clicked the button to dismiss the scanner
     */
    USER_DISMISSED_SCANNER("userDismissedScanner"),

    /**
     * ios: permission was disabled by the user and will need to be turned on in settings
     * android: permission was denied by user when prompt, could ask again
     */
    USER_DENIED_PERMISSION("userDeniedPermission"),

    /**
     * android: permission was denied along "don't ask again" when prompt, will need to go app setting to turn on
     */
    USER_DISABLED_PERMISSION("userDisabledPermissions"),

    /**
     * A hardware or unknown failure happened when trying to use the camera
     * or other reasion, like FirebaseVision failure.
     * This is not caused by a lack of permission.
     */
    UNKNOWN_REASON("unknownReason"),

    /**
     * android only: the hosting activity could be destroyed while scanning is in
     * foreground, as a result the success or failure can't delivered to webview.
     * It could be delivered to hosting activity when recreated after
     * leaving the scanning activity, but not the webview
     */
    BRIDGE_UNAVAILABLE("bridgeUnavailable");

    override fun stringify() = value
}