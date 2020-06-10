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

class BarcodeScannerFailureCodeTest {

    @Test
    fun `verify converting to json`() {
        Assert.assertEquals("userDeniedPermission", USER_DENIED_PERMISSION.stringify())
        Assert.assertEquals("userDisabledPermissions", USER_DISABLED_PERMISSION.stringify())
        Assert.assertEquals("userDismissedScanner", USER_DISMISSED_SCANNER.stringify())
        Assert.assertEquals("unknownReason", UNKNOWN_REASON.stringify())
        Assert.assertEquals("bridgeUnavailable", BRIDGE_UNAVAILABLE.stringify())
    }
}