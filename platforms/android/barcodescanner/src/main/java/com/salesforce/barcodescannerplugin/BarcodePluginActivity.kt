/*
 *
 * Copyright (c) 2019, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import kotlinx.android.synthetic.main.top_action_bar_in_live_camera.*
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BarcodePluginActivity : AppCompatActivity() {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var viewFinder: PreviewView
    private lateinit var executor: ExecutorService

    private lateinit var barcodeAnalyzer: BarcodeAnalyzer
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null

    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barcode_plugin_activity)
        viewFinder = findViewById(R.id.preview_view)

        executor = Executors.newSingleThreadExecutor()

        if (!Utils.arePermissionsGranted(this)) {
            Utils.requestPermissions(this)
        } else {
            viewFinder.post { initializeCamera() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shut down our background executor
        executor.shutdown()

        preview = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Utils.arePermissionsGranted(this)) {
            viewFinder.post { initializeCamera() }
        }
    }

    /**
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun initializeCamera() {
        val barcodeScannerOptions =
            intent.extras?.getSerializable(OPTIONS_VALUE) as BarcodeScannerOptions?

        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        val rotation = viewFinder.display.rotation

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // Camera Selector
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            // Image Preview
            preview = Preview.Builder().apply {
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(rotation)
            }.build()

            preview?.setSurfaceProvider(viewFinder.previewSurfaceProvider)
            barcodeAnalyzer = BarcodeAnalyzer({ qrCodes ->
                if (qrCodes.isNotEmpty()) {
                    val barcode = qrCodes.first()
                    onBarcodeFound(barcode)
                }
            }, barcodeScannerOptions)

            // Image Analysis
            imageAnalysis = ImageAnalysis.Builder()
                // We request aspect ratio but no resolution
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setAnalyzer(
                        executor, barcodeAnalyzer
                    )
                }

            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll()

            try {
                // A variable number of use-cases can be passed here -
                // camera provides access to CameraControl & CameraInfo
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

        close_button.setOnClickListener {
            onBackPressed() }
    }

    private fun onBarcodeFound(barcode: FirebaseVisionBarcode) {
        EventBus.getDefault().postSticky(
            BarcodeScannedEvent(
                BarcodeScannerResult(
                    BarcodeType.fromVisionBarcode(barcode.format),
                    barcode.displayValue ?: ""
                )
            )
        )
        this.finish()
    }

    companion object {
        private const val TAG = "BarcodePluginActivity"
        const val OPTIONS_VALUE = "OptionsValue"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}