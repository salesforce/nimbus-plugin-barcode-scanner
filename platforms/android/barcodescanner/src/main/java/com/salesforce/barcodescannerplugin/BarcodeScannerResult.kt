/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import com.salesforce.nimbus.JSONSerializable
import org.json.JSONObject
import java.net.URLEncoder

data class BarcodeScannerResult(val type: BarcodeType, val value: String) : JSONSerializable {
    override fun stringify() = JSONObject().apply {
        put("type", type)
        put("value", value)
    }.toString()
}
