/*
 *
 * Copyright (c) 2019, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import org.json.JSONObject
import java.io.Serializable

class BarcodeScannerOptions(val barcodeTypes: List<BarcodeType>): Serializable {
    companion object {
        @JvmStatic
        fun fromJSON(barcodeScannerOptions: String): BarcodeScannerOptions {
            val options = JSONObject(barcodeScannerOptions)
            val barcodeTypes = options.getJSONArray("barcodeTypes")
            val convertedTypes = mutableListOf<BarcodeType>()
            for (i in 0 until barcodeTypes.length()) {
                convertedTypes.add(BarcodeType.valueOf(barcodeTypes.getString(i).toUpperCase()))
            }
            return BarcodeScannerOptions(convertedTypes)
        }
    }
}