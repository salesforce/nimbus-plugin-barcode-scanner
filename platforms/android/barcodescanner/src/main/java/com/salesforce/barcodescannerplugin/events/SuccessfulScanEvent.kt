/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin.events

import com.salesforce.barcodescannerplugin.BarcodeScannerResult

/**
 * event posted to event bus when successful barcode is acquired by the plug in
 *
 */
class SuccessfulScanEvent(val barcode: BarcodeScannerResult)
