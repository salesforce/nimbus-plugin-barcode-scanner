package com.salesforce.barcodescannerplugin

import com.salesforce.nimbus.JSONSerializable
import org.json.JSONObject

/**
 * Barcode scanner failure
 */
data class BarcodeScannerFailure(val code: BarcodeScannerFailureCode, val message: String? = null) :
    JSONSerializable {

    override fun stringify() =
        JSONObject().apply {
            put("code", code.stringify())
            putOpt("message", message)
        }.toString()
}