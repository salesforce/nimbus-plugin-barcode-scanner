/*
 *
 * Copyright (c) 2019, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

enum class BarcodeType(val barcodeName: String) : VisionBarcodeEnum{
        CODE128("code128") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_CODE_128
        },
        CODE39("code39") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_CODE_39
        },
        CODE93("code93") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_CODE_93
        },
        DATAMATRIX("datamatrix") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_DATA_MATRIX
        },
        EAN13("ean13") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_EAN_13
        },
        EAN8("ean8") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_EAN_8
        },
        ITF("itf") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_ITF
        },
        UPCA("upca") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_UPC_A
        },
        UPCE("upce") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_UPC_E
        },
        PDF417("pdf417") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_PDF417
        },
        QR("qr") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_QR_CODE
        },
        UNKNOWN("") {
                override fun toVisionBarcodeType(): Int = FirebaseVisionBarcode.FORMAT_UNKNOWN
        };
    companion object {
            fun fromVisionBarcode(visionBarcode: Int): BarcodeType{
                    return when (visionBarcode) {
                            FirebaseVisionBarcode.FORMAT_CODE_128 -> CODE128
                            FirebaseVisionBarcode.FORMAT_CODE_39 -> CODE39
                            FirebaseVisionBarcode.FORMAT_CODE_93 -> CODE93
                            FirebaseVisionBarcode.FORMAT_DATA_MATRIX -> DATAMATRIX
                            FirebaseVisionBarcode.FORMAT_EAN_13 -> EAN13
                            FirebaseVisionBarcode.FORMAT_EAN_8 -> EAN8
                            FirebaseVisionBarcode.FORMAT_ITF -> ITF
                            FirebaseVisionBarcode.FORMAT_UPC_A -> UPCA
                            FirebaseVisionBarcode.FORMAT_UPC_E -> UPCE
                            FirebaseVisionBarcode.FORMAT_PDF417 -> PDF417
                            FirebaseVisionBarcode.FORMAT_QR_CODE -> QR
                            else -> UNKNOWN
                    }

            }
    }
}
interface VisionBarcodeEnum {
        fun toVisionBarcodeType(): Int
}