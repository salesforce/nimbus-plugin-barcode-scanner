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
import kotlinx.serialization.stringify
import org.junit.Assert
import org.junit.Test

class BarcodeScannerFailureCodeTest {

    private val json = Json(JsonConfiguration(encodeDefaults = false))

    @ImplicitReflectionSerializer
    @Test
    fun `serialize correctly`() {
        val result = json.stringify(BarcodeScannerFailureCode.USER_DISMISSED_SCANNER)
        Assert.assertEquals("\"userDismissedScanner\"", result)
    }
}
