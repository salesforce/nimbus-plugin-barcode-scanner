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
import org.junit.Assert.assertEquals
import org.junit.Test

class BarcodeScannerResultTests {
    @Test
    fun `stringify returns type and value`() {
        val json =  BarcodeScannerResult(UPCE, "SimpleCode").stringify()
        assertEquals(json,"""{"type":"UPCE","value":"SimpleCode"}""")
    }

    @Test
    fun `stringify returns can handle xml QR code`() {
        val json = BarcodeScannerResult(QR, """<a href="#">text</a>""").stringify()
        assertEquals(json,"""{"type":"QR","value":"<a href=\"#\">text<\/a>"}""")
    }

    @Test
    fun `stringify returns can handle json QR code`() {
        val json = BarcodeScannerResult(QR, """{"a":1,"b":{"c":true}}}""").stringify()
        assertEquals(json,"""{"type":"QR","value":"{\"a\":1,\"b\":{\"c\":true}}}"}""")
    }
}
