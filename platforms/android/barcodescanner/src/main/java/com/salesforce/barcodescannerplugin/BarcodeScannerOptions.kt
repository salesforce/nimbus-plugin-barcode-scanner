/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import kotlinx.serialization.Serializable

/**
 * the barcode scanner options
 *
 * @Serializable to make json Serializable for kotlin
 * extends Serializable to make it could be passed as activity bundle
 * without extra code
 */
@Serializable
class BarcodeScannerOptions(
    val barcodeTypes: MutableList<BarcodeType> = mutableListOf(),
    val instructionText: String? = null,
    val successText: String? = null
) : java.io.Serializable {

    init {
        // make sure have options and support all barcode types if not specified by client
        if (barcodeTypes.isEmpty()) {
            barcodeTypes.addAll(BarcodeType.values().asList())
        }
    }
}
