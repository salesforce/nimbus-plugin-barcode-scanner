/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import android.content.Context
import android.webkit.WebView
import android.widget.Toast
import com.salesforce.barcodescannerplugin.events.*
import com.salesforce.nimbus.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@PluginOptions("barcodeScanner")
class BarcodeScannerPlugin(private val context: Context) : Plugin, BarcodeScanner {

    private var scannerCallback: ((barcode: BarcodeScannerResult?, failure: BarcodeScannerFailure?) -> Unit)? =
        null
    private lateinit var barcodeOptions: BarcodeScannerOptions
    private val eventBus = EventBus.getDefault()

    init {
        registerEventBus()
    }

    @BoundMethod
    override fun beginCapture(
        options: BarcodeScannerOptions?,
        callback: (barcode: BarcodeScannerResult?, failure: BarcodeScannerFailure?) -> Unit
    ) {
        barcodeOptions = options ?: BarcodeScannerOptions(listOf())
        scannerCallback = callback
        startScanner()
    }

    @BoundMethod
    override fun resumeCapture(callback: (barcode: BarcodeScannerResult?, failure: BarcodeScannerFailure?) -> Unit) {
        scannerCallback = callback
        startScanner()
    }

    /**
     * end capturing:
     * 1. message out StopScanEvent so to activity listens it and quit
     * 2. unregister itself from event bus.
     */
    @BoundMethod
    override fun endCapture() {
        eventBus.apply {
            post(StopScanEvent())
            unregister(this)
        }
    }

    @Subscribe
    fun onMessage(event: ScanStartedEvent) = registerEventBus()

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessage(event: SuccessfulScanEvent) {
        eventBus.removeStickyEvent(event)
        if (scannerCallback != null) {
            scannerCallback?.invoke(event.barcode, null)
        } else {
            showBridgeBrokenMessage()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessage(event: FailedScanEvent) {
        eventBus.removeStickyEvent(event)
        if (scannerCallback != null) {
            scannerCallback?.invoke(
                null,
                BarcodeScannerFailure(event.errorCode, event.exception?.toString())
            )
        } else {
            showBridgeBrokenMessage()
        }
    }

    override fun cleanup(webView: WebView, bridge: Bridge) = unRegisterEventBus()


    /**
     * launch the BarcodePluginActivity to do the scanning
     */
    private fun startScanner() {
        registerEventBus()
        context.startActivity(
            BarcodePluginActivity.getIntent(context.applicationContext, barcodeOptions)
        )
    }

    /**
     * register the plugin to event bus if haven't done so
     */
    private fun registerEventBus() {
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this)
        }
    }

    private fun unRegisterEventBus() = eventBus.unregister(this)

    private fun showBridgeBrokenMessage() {
        Toast.makeText(context, R.string.bridge_broken_message, Toast.LENGTH_LONG).show()
        eventBus.post(StopScanEvent())
    }

}
