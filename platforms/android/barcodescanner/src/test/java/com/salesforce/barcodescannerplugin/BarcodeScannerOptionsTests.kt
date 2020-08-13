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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class BarcodeScannerOptionsTests {
    @Test
    fun `convert json to barcodeScannerOptions`() {
        val originalOptions = """
            {'barcodeTypes': 
                ['code128', 'code39', 'upca', 'qr'],
             'instructionText': 'position properly',
             'successText': 'found a barcode'
            }
        """.trimIndent()
        val options = BarcodeScannerOptions.fromJSON(originalOptions)
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
        val convertedType = BarcodeScannerOptions.fromJSON("")
        assertNotNull(convertedType)
    }

    @Test
    fun `passing no barcode types defaults to empty list`() {
        val barcodeScannerOptions = BarcodeScannerOptions()
        assert(barcodeScannerOptions.barcodeTypes.count() == 0)
    }

    @Test
    fun `convert json to barcodeScannerOptions with all barcode types when the input array is empty`() {
        val originalOptions = """
            {'barcodeTypes': 
                []
            }
        """.trimIndent()
        val barcodeScannerOptions = BarcodeScannerOptions.fromJSON(originalOptions)
        assert(barcodeScannerOptions.barcodeTypes.count() == enumValues<BarcodeType>().count())
    }

    @Test
    fun `default instruction and success text as empty string`() {
        val options = BarcodeScannerOptions.fromJSON(
            """
            {'barcodeTypes': 
                []
            }
        """
        )
        assertEquals("", options.instructionText);
        assertEquals("", options.successText);
    }
}
