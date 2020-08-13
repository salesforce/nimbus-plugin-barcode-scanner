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

class BarcodeScannerOptions(
    val barcodeTypes: List<BarcodeType> = listOf(),
    val instructionText: String = "",
    val successText: String = ""
    ) : Serializable {
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
                val instructionText = options.optString("instructionText", "")
                val successText = options.optString("successText", "")
                BarcodeScannerOptions(convertedTypes, instructionText, successText)
            } catch (e: Exception) {
                BarcodeScannerOptions()
            }
        }
    }
}
