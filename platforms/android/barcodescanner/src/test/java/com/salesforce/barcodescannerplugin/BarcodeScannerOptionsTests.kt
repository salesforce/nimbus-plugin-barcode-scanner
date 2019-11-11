/*
 *
 * Copyright (c) 2019, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class BarcodeScannerOptionsTests {
    @Test
    fun `convert json to barcodeScannerOptions`() {
        val originalOptions = """
            {'barcodeTypes': 
                ['code128', 'code39', 'upca', 'qr']
            }
        """.trimIndent()
        val convertedType = BarcodeScannerOptions.fromJSON(originalOptions)
        assertEquals(listOf(BarcodeType.CODE128, BarcodeType.CODE39, BarcodeType.UPCA, BarcodeType.QR), convertedType.barcodeTypes)
    }

    @Test
    fun `null json to barcodeScannerOptions`() {
        val convertedType = BarcodeScannerOptions.fromJSON("")
        assertNotNull(convertedType)
    }
}
