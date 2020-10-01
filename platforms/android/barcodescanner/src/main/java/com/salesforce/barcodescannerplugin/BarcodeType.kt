/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BarcodeType {
    @SerialName("code128")
    CODE128,

    @SerialName("code39")
    CODE39,

    @SerialName("code93")
    CODE93,

    @SerialName("datamatrix")
    DATAMATRIX,

    @SerialName("ean13")
    EAN13,

    @SerialName("ean8")
    EAN8,

    @SerialName("itf")
    ITF,

    @SerialName("upca")
    UPCA,

    @SerialName("upce")
    UPCE,

    @SerialName("pdf417")
    PDF417,

    @SerialName("qr")
    QR,

    @SerialName("unknown")
    UNKNOWN;

    fun toVisionBarcodeType() =
        barcodeMap.filterValues { barcode -> barcode == this }.keys.first()

    companion object {
        val barcodeMap: Map<Int, BarcodeType> = mapOf(

            FirebaseVisionBarcode.FORMAT_CODE_128 to CODE128,
            FirebaseVisionBarcode.FORMAT_CODE_39 to CODE39,
            FirebaseVisionBarcode.FORMAT_CODE_93 to CODE93,
            FirebaseVisionBarcode.FORMAT_DATA_MATRIX to DATAMATRIX,
            FirebaseVisionBarcode.FORMAT_EAN_13 to EAN13,
            FirebaseVisionBarcode.FORMAT_EAN_8 to EAN8,
            FirebaseVisionBarcode.FORMAT_ITF to ITF,
            FirebaseVisionBarcode.FORMAT_UPC_A to UPCA,
            FirebaseVisionBarcode.FORMAT_UPC_E to UPCE,
            FirebaseVisionBarcode.FORMAT_PDF417 to PDF417,
            FirebaseVisionBarcode.FORMAT_QR_CODE to QR,
            FirebaseVisionBarcode.TYPE_UNKNOWN to UNKNOWN
        )

        fun fromVisionBarcode(visionBarcode: Int): BarcodeType {
            return barcodeMap[visionBarcode] ?: UNKNOWN
        }
    }
}
