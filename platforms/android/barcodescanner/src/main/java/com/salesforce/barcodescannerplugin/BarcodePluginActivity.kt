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
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.salesforce.barcodescannerplugin.events.FailedScanEvent
import com.salesforce.barcodescannerplugin.events.ScanStartedEvent
import com.salesforce.barcodescannerplugin.events.StopScanEvent
import com.salesforce.barcodescannerplugin.events.SuccessfulScanEvent
import kotlinx.android.synthetic.main.top_action_bar_in_live_camera.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class BarcodePluginActivity : AppCompatActivity() {

    private lateinit var viewFinder: BarcodeScannerPreviewView
    private val eventBus = EventBus.getDefault()
    private lateinit var mainHandler: Handler
    private lateinit var barcodeAnalyzer: BarcodeAnalyzer

    /** if successful scan event is not processed timely, mostly the bridge between the plugin and
     * the web view is broken due to activity destroy/recreate, run this runnable to finish
     * the scanning activity */
    private var eventMessageDeliveryCheckRunnable = Runnable {
        if (eventBus.getStickyEvent(SuccessfulScanEvent::class.java) != null) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barcode_plugin_activity)

        mainHandler = Handler(Looper.getMainLooper())
        viewFinder = findViewById(R.id.preview_view)

        barcodeAnalyzer = BarcodeAnalyzer(
            { qrCodes ->
                if (qrCodes.isNotEmpty()) {
                    barcodeAnalyzer.isPaused = true
                    val barcode = qrCodes.first()
                    onBarcodeFound(barcode)
                }
            },
            {
                scanFailed(BarcodeScannerFailureCode.UNKNOWN_REASON, it)
            },
            intent.extras?.getSerializable(OPTIONS_VALUE) as BarcodeScannerOptions?
        )

        close_button.setOnClickListener {
            onBackPressed()
        }

        if (Utils.arePermissionsGranted(this)) {
            startScan()
        } else {
            Utils.requestPermissions(this)
        }

        eventBus.register(this)
        eventBus.post(ScanStartedEvent())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        barcodeAnalyzer?.isPaused = false
    }

    override fun onDestroy() {
        // call preview onDestroy and shutdown executor
        viewFinder.onDestroy()
        mainHandler.removeCallbacks(eventMessageDeliveryCheckRunnable)
        eventBus.unregister(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        scanFailed(BarcodeScannerFailureCode.USER_DISMISSED_SCANNER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Utils.arePermissionsGranted(this)) {
            // permission granted, start scan
            startScan()
        } else {
            // denied, notify
            scanFailed(
                if (Utils.shouldShowRequestPermissionRationale(this))
                    BarcodeScannerFailureCode.USER_DENIED_PERMISSION
                else
                    BarcodeScannerFailureCode.USER_DISABLED_PERMISSION
            )
        }
    }

    /**
     * when received StopScanEvent, finish the activity
     */
    @Subscribe
    fun onMessage(event: StopScanEvent) = finish()

    private fun onBarcodeFound(barcode: FirebaseVisionBarcode) {
        // only notify SuccessfulScan when activity is resumed
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            eventBus.postSticky(
                SuccessfulScanEvent(
                    BarcodeScannerResult(
                        BarcodeType.fromVisionBarcode(barcode.format),
                        barcode.displayValue ?: ""
                    )
                )
            )
            // check the successful scan to be consumed timely
            mainHandler.postDelayed(
                eventMessageDeliveryCheckRunnable,
                SUCCESSFUL_SCAN_PROCESS_TIME_THRESHOLD_IN_MS
            )
        }
    }

    private fun startScan() {
        mainHandler.post {
            try {
                viewFinder.startScan(this, barcodeAnalyzer)
            } catch (exc: Exception) {
                scanFailed(BarcodeScannerFailureCode.UNKNOWN_REASON, exc)
            }
        }
    }

    private fun scanFailed(code: BarcodeScannerFailureCode, exception: Exception? = null) {
        finish()
        eventBus.postSticky(FailedScanEvent(code, exception))
    }

    companion object {
        private const val OPTIONS_VALUE = "OptionsValue"
        private const val SUCCESSFUL_SCAN_PROCESS_TIME_THRESHOLD_IN_MS = 1000L

        /**
         * create the intent for launch BarcodePluginActivity, set intent flag to SINGLE_TOP to only allow one BarcodePluginActivity
         *
         * @param context the context for the intent
         * @param barcodeOptions
         */
        fun getIntent(context: Context, barcodeOptions: BarcodeScannerOptions) =
            Intent(context, BarcodePluginActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putSerializable(OPTIONS_VALUE, barcodeOptions)
                })
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
    }
}
