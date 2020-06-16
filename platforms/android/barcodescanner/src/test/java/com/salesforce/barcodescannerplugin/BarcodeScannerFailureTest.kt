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
import org.junit.Assert
import org.junit.Test
import java.util.*

class BarcodeScannerFailureTest {
    @Test
    fun `verify converting to json string`() {
        val error = BarcodeScannerFailure(USER_DISMISSED_SCANNER).stringify()
        Assert.assertEquals("{\"code\":\"${USER_DISMISSED_SCANNER.value}\"}", error)
    }

    @Test
    fun `verify converting to json string with message`() {
        val messageBody = UUID.randomUUID().toString()
        val error = BarcodeScannerFailure(UNKNOWN_REASON, messageBody).stringify()
        Assert.assertEquals(
            "{\"code\":\"${UNKNOWN_REASON.value}\",\"message\":\"$messageBody\"}",
            error
        )
    }
}