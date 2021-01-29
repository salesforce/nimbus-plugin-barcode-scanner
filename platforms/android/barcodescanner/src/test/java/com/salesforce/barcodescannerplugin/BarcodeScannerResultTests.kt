/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import com.salesforce.barcodescannerplugin.BarcodeType.QR
import com.salesforce.barcodescannerplugin.BarcodeType.UPCE
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Test

class BarcodeScannerResultTests {
    private val json =  Json { encodeDefaults = false }

    @Test
    fun `stringify returns type and value`() {
        assertEquals(
            """{"type":"upce","value":"SimpleCode"}""",
            json.encodeToString(BarcodeScannerResult(UPCE, "SimpleCode"))
        )
    }

    @Test
    fun `stringify returns can handle xml QR code`() {
        assertEquals(
            """{"type":"qr","value":"<a href=\"#\">text</a>"}""",
            json.encodeToString(BarcodeScannerResult(QR, """<a href="#">text</a>"""))
        )
    }

    @Test
    fun `stringify returns can handle json QR code`() {
        assertEquals(
            """{"type":"qr","value":"{\"a\":1,\"b\":{\"c\":true}}}"}""",
            json.encodeToString(BarcodeScannerResult(QR, """{"a":1,"b":{"c":true}}}"""))
        )
    }
}
