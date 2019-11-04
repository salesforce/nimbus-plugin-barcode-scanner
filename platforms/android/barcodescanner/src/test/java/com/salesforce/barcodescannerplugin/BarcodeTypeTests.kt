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
import org.junit.Test

class BarcodeTypeTests {
    @Test
    fun `toVisionBarcodeType returns correct value`() {
        val barcodeType = BarcodeType.EAN8
        val visionType = barcodeType.toVisionBarcodeType()
        assert(visionType == FirebaseVisionBarcode.FORMAT_EAN_8)
    }

    @Test
    fun `fromVisionBarcode returns correct barcodeType`() {
        val visionType = FirebaseVisionBarcode.FORMAT_CODE_128
        val barcodeType = BarcodeType.fromVisionBarcode(visionType)
        assert(barcodeType == BarcodeType.CODE128)
    }
}
