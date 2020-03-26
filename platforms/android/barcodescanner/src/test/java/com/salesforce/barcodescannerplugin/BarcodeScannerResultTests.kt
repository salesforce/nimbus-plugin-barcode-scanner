/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import org.junit.Test
import java.util.UUID

class BarcodeScannerResultTests {
    @Test
    fun `stringify returns type and value`() {
        val barcodeScannerResult = BarcodeScannerResult(BarcodeType.UPCE, UUID.randomUUID().toString())
        val stringified = barcodeScannerResult.stringify()
        assert(stringified.contains("\"type\":\"${barcodeScannerResult.type}\""))
        assert(stringified.contains("\"value\":\"${barcodeScannerResult.value}\""))
    }
}
