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

class BarcodeScannerFailureTest {
    @Test
    fun `verify converting to json string`() {
        val error = BarcodeScannerFailure(USER_DISMISSED_SCANNER).stringify()
        Assert.assertEquals("{\"code\":\"userDismissedScanner\"}", error)
    }

    @Test
    fun `verify converting to json string with message`() {
        val error = BarcodeScannerFailure(UNKNOWN_REASON, "message body").stringify()
        Assert.assertEquals("{\"code\":\"unknownReason\",\"message\":\"message body\"}", error)
    }
}