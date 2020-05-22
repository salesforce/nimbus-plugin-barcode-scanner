package com.salesforce.barcodescannerplugin

data class BarcodeScannerFailure(val code: BarcodeScannerFailureCode, val message: String? = null)