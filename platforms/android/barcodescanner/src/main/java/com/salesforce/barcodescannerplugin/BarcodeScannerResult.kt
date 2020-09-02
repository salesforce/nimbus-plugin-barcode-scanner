/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import kotlinx.serialization.Serializable

@Serializable
data class BarcodeScannerResult(val type: BarcodeType, val value: String)
