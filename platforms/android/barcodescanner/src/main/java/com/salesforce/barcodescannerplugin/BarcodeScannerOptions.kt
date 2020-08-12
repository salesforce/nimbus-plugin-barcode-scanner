/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import org.json.JSONObject
import java.io.Serializable
import java.lang.Exception

class BarcodeScannerOptions(val barcodeTypes: List<BarcodeType> = listOf()) : Serializable {
    companion object {
        @JvmStatic
        fun fromJSON(barcodeScannerOptions: String): BarcodeScannerOptions {
            return try {
                val options = JSONObject(barcodeScannerOptions)
                val barcodeTypes = options.getJSONArray("barcodeTypes")
                var convertedTypes = mutableListOf<BarcodeType>()
                if (barcodeTypes.length() > 0) {
                    for (i in 0 until barcodeTypes.length()) {
                        convertedTypes.add(BarcodeType.valueOf(barcodeTypes.getString(i).toUpperCase()))
                    }
                } else {
                    convertedTypes = enumValues<BarcodeType>().toMutableList()
                }
                BarcodeScannerOptions(convertedTypes)
            } catch (e: Exception) {
                BarcodeScannerOptions()
            }
        }
    }
}
