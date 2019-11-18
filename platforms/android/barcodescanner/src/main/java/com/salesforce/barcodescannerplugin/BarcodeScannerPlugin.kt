/*
 *
 * Copyright (c) 2019, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.salesforce.barcodescannerplugin.barcodedetection.BarcodeErrorEvent
import com.salesforce.barcodescannerplugin.barcodedetection.BarcodeScannedEvent
import com.salesforce.nimbus.Extension
import com.salesforce.nimbus.ExtensionMethod
import com.salesforce.nimbus.NimbusExtension
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Extension("barcodeScanner")
class BarcodeScannerPlugin(val activity: Activity) : NimbusExtension, BarcodeScanner {
    private lateinit var scannerCallback: (barcode: BarcodeScannerResult?, error: String?) -> Unit
    private lateinit var barcodeOptions: BarcodeScannerOptions

    @ExtensionMethod
    override fun beginCapture(
        options: BarcodeScannerOptions?,
        callback: (barcode: BarcodeScannerResult?, error: String?) -> Unit
    ) {
        barcodeOptions = options ?: BarcodeScannerOptions(listOf())
        scannerCallback = callback
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        startScanner()
    }

    @ExtensionMethod
    override fun resumeCapture() {
        startScanner()
    }

    @ExtensionMethod
    override fun endCapture() {
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BarcodeScannedEvent) {
        scannerCallback(event.barcode, null)
        EventBus.getDefault().removeStickyEvent(event)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BarcodeErrorEvent) {
        scannerCallback(null, event.errorMessage)
        EventBus.getDefault().removeStickyEvent(event)
    }

    fun startScanner() {
        val intent = Intent(activity, LiveBarcodeScanningActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(LiveBarcodeScanningActivity.OPTIONS_VALUE, barcodeOptions)
        intent.putExtras(bundle)
        activity.startActivity(intent)
    }
}
