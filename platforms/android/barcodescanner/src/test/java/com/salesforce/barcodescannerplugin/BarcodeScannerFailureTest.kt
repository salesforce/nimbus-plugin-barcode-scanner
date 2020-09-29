/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */
package com.salesforce.barcodescannerplugin

import com.salesforce.barcodescannerplugin.BarcodeScannerFailureCode.*
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.stringify
import org.junit.Assert
import org.junit.Test
import java.util.*

@ImplicitReflectionSerializer
class BarcodeScannerFailureTest {
    private val json = Json(JsonConfiguration(encodeDefaults = false))

    @Test
    fun `verify converting to json string`() {
        val error = json.stringify(BarcodeScannerFailure(USER_DISMISSED_SCANNER))
        Assert.assertEquals("{\"code\":\"userDismissedScanner\"}", error)
    }

    @Test
    fun `verify converting to json string with message`() {
        val messageBody = UUID.randomUUID().toString()
        val error =  json.stringify(BarcodeScannerFailure(UNKNOWN_REASON, messageBody))
        Assert.assertEquals(
            "{\"code\":\"unknownReason\",\"message\":\"$messageBody\"}",
            error
        )
    }
}