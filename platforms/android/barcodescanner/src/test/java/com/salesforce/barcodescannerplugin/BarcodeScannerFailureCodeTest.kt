/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */
package com.salesforce.barcodescannerplugin

import org.junit.Assert
import org.junit.Test

class BarcodeScannerFailureCodeTest {

    @Test
    fun `stringify returns value`() {
        BarcodeScannerFailureCode.values().forEach {
            Assert.assertEquals(it.value, it.stringify())
        }
    }
}