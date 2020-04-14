/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.salesforce.nimbus.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@PluginOptions("barcodeScanner")
class BarcodeScannerPlugin(private val activity: AppCompatActivity) : NimbusExtension, BarcodeScanner {
    private lateinit var scannerCallback: (barcode: BarcodeScannerResult?, error: String?) -> Unit
    private lateinit var barcodeOptions: BarcodeScannerOptions

    @BoundMethod
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

    @BoundMethod
    override fun resumeCapture() {
        startScanner()
    }

    @BoundMethod
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

    private fun startScanner() {
        activity.supportFragmentManager
        val intent = Intent(activity, BarcodePluginActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(BarcodePluginActivity.OPTIONS_VALUE, barcodeOptions)
        intent.putExtras(bundle)
        activity.startActivity(intent)
    }
}
