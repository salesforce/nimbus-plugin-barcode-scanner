/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin.events

import java.lang.Exception

/**
 * event posted to event bus when failed to scan a barcode.
 *
 * @param errorMessage error message for the failed scan
 */
class FailedScanEvent(val errorMessage: String, val exception: Exception)
