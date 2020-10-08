/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.parse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

@ImplicitReflectionSerializer
class BarcodeScannerOptionsTests {
    private val json = Json(JsonConfiguration(encodeDefaults = false))

    @Test
    fun `convert json to barcodeScannerOptions`() {
        val originalOptions = """
            {"barcodeTypes": 
                ["code128", "code39", "upca", "qr"],
             "instructionText": "position properly",
             "successText": "found a barcode"
            }
        """.trimIndent()
        val options = json.parse<BarcodeScannerOptions>(originalOptions)
        assertEquals(
            listOf(
                BarcodeType.CODE128,
                BarcodeType.CODE39,
                BarcodeType.UPCA,
                BarcodeType.QR
            ), options.barcodeTypes
        )

        assertEquals("position properly", options.instructionText)
        assertEquals("found a barcode", options.successText)
    }

    @Test
    fun `null json to barcodeScannerOptions`() {
        val convertedType = json.parse<BarcodeScannerOptions>("{}")
        assertNotNull(convertedType)
    }

    @Test
    fun `passing no barcode types defaults to empty list`() {
        val barcodeScannerOptions = BarcodeScannerOptions()
        assertEquals(BarcodeType.values().size, barcodeScannerOptions.barcodeTypes.count())
    }

    @Test
    fun `convert json to barcodeScannerOptions with empty barcode types`() {
        val originalOptions = """
            {}
        """.trimIndent()
        val barcodeScannerOptions = json.parse<BarcodeScannerOptions>(originalOptions)
        assertEquals(BarcodeType.values().size, barcodeScannerOptions.barcodeTypes.size)
    }

    @Test
    fun `default instruction and success text to null`() {
        val options = json.parse<BarcodeScannerOptions>(
            """
            {"barcodeTypes": 
                []
            }
        """
        )
        assertNull(options.instructionText)
        assertNull(options.successText)
    }
}
