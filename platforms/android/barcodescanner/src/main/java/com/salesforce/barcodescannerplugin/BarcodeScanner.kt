/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

/**
 * defines barcode scanner capabilities
 */
interface BarcodeScanner {
    fun beginCapture(
        options: BarcodeScannerOptions?,
        callback: (barcode: BarcodeScannerResult?, error: String?) -> Unit
    )

    fun resumeCapture(
        callback: (barcode: BarcodeScannerResult?, error: String?) -> Unit
    )

    fun endCapture()
}
