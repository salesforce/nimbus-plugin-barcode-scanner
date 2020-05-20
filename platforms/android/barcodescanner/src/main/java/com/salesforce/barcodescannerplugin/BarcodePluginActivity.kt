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
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
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
            // denied permission, further explanation?
            if (Utils.shouldShowRequestPermissionRationale(this)) {
                // show explanation and hope user will grant it
                showAlertDialog(R.string.camera_permission_explanation,
                    OnClickListener { _, _ -> Utils.requestPermissions(this) }
                )
            } else {
                // camera permission disabled,  prompt to go app setting to enable it
                showAlertDialog(
                    R.string.enable_camera_permission_explanation,
                    OnClickListener { _, _ -> openAppSetting() }
                )
            }
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
        mainHandler.post { viewFinder.startScan(this, barcodeAnalyzer) }
    }

    /**
     * open app setting screen for current app, ideally open into the permission section, but seems not possible
     */
    private fun openAppSetting() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        )
        finish()
    }

    /**
     *
     */
    private fun showAlertDialog(content: Int, callback: OnClickListener) {
        AlertDialog.Builder(this)
            .setPositiveButton(android.R.string.ok, callback)
            .setMessage(content)
            .create()
            .show()
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
                putExtras(Bundle().apply { putSerializable(OPTIONS_VALUE, barcodeOptions) })
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
    }
}
