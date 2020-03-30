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

enum class BarcodeType {
        CODE128,
        CODE93,
        CODE39,
        DATAMATRIX,
        EAN13,
        EAN8,
        ITF,
        UPCA,
        UPCE,
        PDF417,
        QR,
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
