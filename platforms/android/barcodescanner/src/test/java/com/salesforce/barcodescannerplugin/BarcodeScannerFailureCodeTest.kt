/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */
package com.salesforce.barcodescannerplugin

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test

class BarcodeScannerFailureCodeTest {

    private val json =  Json { encodeDefaults = false }

    @Test
    fun `serialize correctly`() {
        val result = json.encodeToString(BarcodeScannerFailureCode.USER_DISMISSED_SCANNER)
        Assert.assertEquals("\"userDismissedScanner\"", result)
    }
}
