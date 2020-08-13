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
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageProxy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.salesforce.barcodescannerplugin.events.FailedScanEvent
import com.salesforce.barcodescannerplugin.events.ScanStartedEvent
import com.salesforce.barcodescannerplugin.events.StopScanEvent
import com.salesforce.barcodescannerplugin.events.SuccessfulScanEvent
import kotlinx.android.synthetic.main.barcode_plugin_activity.*
import kotlinx.android.synthetic.main.top_action_bar_in_live_camera.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class BarcodePluginActivity : AppCompatActivity() {

    private lateinit var viewFinder: BarcodeScannerPreviewView
    private lateinit var statusText: TextView
    private lateinit var scanningIndicator: View
    private lateinit var scanSuccessIndicator: View
    private lateinit var capturedBarcodeImagePreview: ImageView
    private lateinit var frozenFrame: ImageView
    private val eventBus = EventBus.getDefault()
    private lateinit var mainHandler: Handler
    private var barcodeScannerOptions: BarcodeScannerOptions? = null
    private lateinit var barcodeAnalyzer: BarcodeAnalyzer

    /** if successful scan event is not processed timely, mostly the bridge between the plugin and
     * the web view is broken due to activity destroy/recreate, run this runnable to finish
     * the scanning activity */
    private val eventMessageDeliveryCheckRunnable = Runnable {
        if (eventBus.getStickyEvent(SuccessfulScanEvent::class.java) != null) {
            finish()
        }
    }

    /**
     * runnable to hide the ml loading indicator
     */
    private val hideLoadingIndicatorRunnable = Runnable {
        firebase_ml_loading_indicator.visibility = GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barcode_plugin_activity)

        mainHandler = Handler(Looper.getMainLooper())
        viewFinder = findViewById(R.id.preview_view)
        statusText = findViewById(R.id.status_text)
        scanningIndicator = findViewById(R.id.scanning_indicator)
        scanSuccessIndicator = findViewById(R.id.scan_success_indicator)
        capturedBarcodeImagePreview = findViewById(R.id.captured_barcode_image_preview)
        frozenFrame = findViewById(R.id.frozen_frame)
        lifecycle.addObserver(viewFinder)

        barcodeScannerOptions =
            intent.extras?.getSerializable(OPTIONS_VALUE) as BarcodeScannerOptions?
        barcodeAnalyzer = BarcodeAnalyzer(
            this,
            { qrCodes, image ->
                firebase_ml_loading_indicator.visibility = GONE
                if (qrCodes.isNotEmpty()) {
                    barcodeAnalyzer.isPaused = true
                    val barcode = qrCodes.first()
                    onBarcodeFound(barcode, image)
                }
            },
            {
                onBarcodeDetectFailed(it)
            },
            barcodeScannerOptions
        )

        close_button.setOnClickListener {
            onBackPressed()
        }

        eventBus.register(this)
    }

    override fun onResume() {
        super.onResume()

        if (Utils.arePermissionsGranted(this)) {
            startScan()
        } else {
            Utils.requestPermissions(this)
        }

        eventBus.post(ScanStartedEvent())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        barcodeAnalyzer.isPaused = false
    }

    override fun onDestroy() {
        // call preview onDestroy and shutdown executor
        mainHandler.apply {
            removeCallbacks(eventMessageDeliveryCheckRunnable)
            removeCallbacks(hideLoadingIndicatorRunnable)
        }
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

    private fun onBarcodeFound(barcode: FirebaseVisionBarcode, image: ImageProxy) {
        // only notify SuccessfulScan when activity is resumed
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            updateViewsForScanSuccess(barcode, image)
            mainHandler.postDelayed({
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
            }, ARTIFICIAL_PAUSE_IN_MS)
        }
    }

    private fun onBarcodeDetectFailed(exception: Exception) {
        // if google play service doesn't have the ml model for vision, FirebaseMLException is thrown
        // with message 'Waiting for the barcode detection model to be downloaded. Please wait'
        // so detect such case and show and loading indicator
        if (exception is FirebaseMLException &&
            exception.message != null &&
            exception.message!!.contains("Please wait")
        ) {
            firebase_ml_loading_indicator.visibility = VISIBLE
            mainHandler.apply {
                removeCallbacks(hideLoadingIndicatorRunnable)
                postDelayed(
                    hideLoadingIndicatorRunnable,
                    FIREBASE_ML_LOADING_TIME_THRESHOLD_IN_MS
                )
            }
        } else {
            firebase_ml_loading_indicator.visibility = GONE
            scanFailed(BarcodeScannerFailureCode.UNKNOWN_REASON, exception)
        }
    }

    private fun startScan() {
        mainHandler.post {
            try {
                viewFinder.startScan(this, barcodeAnalyzer)
                updateViewsForStartScan()
            } catch (exc: Exception) {
                scanFailed(BarcodeScannerFailureCode.UNKNOWN_REASON, exc)
            }
        }
    }

    private fun scanFailed(code: BarcodeScannerFailureCode, exception: Exception? = null) {
        finish()
        eventBus.postSticky(FailedScanEvent(code, exception))
    }

    private fun updateViewsForStartScan() {
        if (barcodeScannerOptions?.instructionText!!.isNotEmpty()) {
            statusText.visibility = VISIBLE
            statusText.text = barcodeScannerOptions?.instructionText
        }
        scanningIndicator.visibility = VISIBLE
        scanSuccessIndicator.visibility = GONE
        capturedBarcodeImagePreview.visibility = GONE
        frozenFrame.visibility = GONE
    }

    private fun updateViewsForScanSuccess(barcode: FirebaseVisionBarcode, image: ImageProxy) {
        if (barcodeScannerOptions?.successText!!.isNotEmpty()) {
            statusText.text = barcodeScannerOptions!!.successText
            statusText.visibility = VISIBLE
        } else {
            statusText.visibility = GONE
        }

        scanningIndicator.visibility = GONE
        scanSuccessIndicator.visibility = VISIBLE

        val previewBitmap = Utils.convertImageToBitmap(image)
        frozenFrame.setImageBitmap(previewBitmap)
        frozenFrame.visibility = VISIBLE

        barcode.boundingBox?.apply {
            // resize the barcode region indicator and move to where the barcode is
            val bounds =
                this.extendRectBy(resources.getDimensionPixelSize(R.dimen.bounding_box_padding))
                    .scaleRectBy(getPreviewToImageXRation(image), getPreviewToImageYRation(image))

            capturedBarcodeImagePreview.translationX = bounds.left.toFloat()
            capturedBarcodeImagePreview.translationY = bounds.top.toFloat()
            capturedBarcodeImagePreview.layoutParams = ConstraintLayout.LayoutParams(
                bounds.right - bounds.left,
                bounds.bottom - bounds.top
            )
            capturedBarcodeImagePreview.visibility = VISIBLE

            val cropRect = Rect(this).apply {
                this.intersect(0, 0, previewBitmap.width, previewBitmap.height) }
            val croppedBmp = Bitmap.createBitmap(
                previewBitmap,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height()
            )
            capturedBarcodeImagePreview.setImageBitmap(croppedBmp)
        }
    }

    /**
     * the camera in phone is landscape and this activity is locked in portrait, so the width of activity view is the height of camera image for 0 or 270 rotation
     */
    private fun getPreviewToImageXRation(image: ImageProxy): Float {
        val width =
            if (image.imageInfo.rotationDegrees == 0 || image.imageInfo.rotationDegrees == 270) image.width else image.height
        return viewFinder.width.toFloat() / width
    }

    /**
     * the camera in phone is landscape and this activity is locked in portrait, so the height of activity view is the width of camera image for 0 or 270 rotation
     */
    private fun getPreviewToImageYRation(image: ImageProxy): Float {
        val height =
            if (image.imageInfo.rotationDegrees == 0 || image.imageInfo.rotationDegrees == 270) image.height else image.width
        return viewFinder.height.toFloat() / height
    }


    companion object {
        private const val OPTIONS_VALUE = "OptionsValue"
        private const val SUCCESSFUL_SCAN_PROCESS_TIME_THRESHOLD_IN_MS = 1000L
        private const val FIREBASE_ML_LOADING_TIME_THRESHOLD_IN_MS = 1000L
        private const val ARTIFICIAL_PAUSE_IN_MS = 500L
        private const val STATE_KEY_SCAN_STARTED = "ScanStarted"

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
