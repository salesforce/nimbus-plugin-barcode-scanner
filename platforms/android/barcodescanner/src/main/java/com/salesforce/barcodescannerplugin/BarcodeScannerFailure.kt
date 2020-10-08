package com.salesforce.barcodescannerplugin

import kotlinx.serialization.Serializable

/**
 * Barcode scanner failure
 */
@Serializable
data class BarcodeScannerFailure(
    val code: BarcodeScannerFailureCode,
    val message: String? = null
)
