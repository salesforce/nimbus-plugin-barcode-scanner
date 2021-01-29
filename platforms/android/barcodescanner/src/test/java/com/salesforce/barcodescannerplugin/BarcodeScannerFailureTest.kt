/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */
package com.salesforce.barcodescannerplugin

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.junit.Assert
import org.junit.Test
import java.util.UUID

class BarcodeScannerFailureTest {
    private val json =  Json { encodeDefaults = false }

    @Test
    fun `verify converting to json string`() {
        val error = json.encodeToString(BarcodeScannerFailure(BarcodeScannerFailureCode.USER_DISMISSED_SCANNER))
        Assert.assertEquals("{\"code\":\"userDismissedScanner\"}", error)
    }

    @Test
    fun `verify converting to json string with message`() {
        val messageBody = UUID.randomUUID().toString()
        val error = json.encodeToString(BarcodeScannerFailure(BarcodeScannerFailureCode.UNKNOWN_REASON, messageBody))
        Assert.assertEquals(
            "{\"code\":\"unknownReason\",\"message\":\"$messageBody\"}",
            error
        )
    }
}
