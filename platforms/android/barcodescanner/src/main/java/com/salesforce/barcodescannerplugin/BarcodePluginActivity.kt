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
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.common.util.concurrent.ListenableFuture
import com.salesforce.barcodescannerplugin.barcodedetection.BarcodeAnalyzer
import com.salesforce.barcodescannerplugin.camera.GraphicOverlay
import com.salesforce.barcodescannerplugin.databinding.BarcodePluginActivityBinding
import kotlinx.android.synthetic.main.top_action_bar_in_live_camera.close_button
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BarcodePluginActivity : AppCompatActivity() {
    private lateinit var binding: BarcodePluginActivityBinding

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var imagePreview: Preview
    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private lateinit var imageAnalysis: ImageAnalysis
    private val executor = Executors.newSingleThreadExecutor()
    private var graphicOverlay: GraphicOverlay? = null

    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.barcode_plugin_activity)
        binding.lifecycleOwner = this
        previewView = binding.previewView
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        graphicOverlay = findViewById(R.id.camera_preview_graphic_overlay)

        if (!Utils.arePermissionsGranted(this)) {
            Utils.requestPermissions(this)
        } else {
            previewView.post { initializeCamera() }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Utils.arePermissionsGranted(this)){
            previewView.post { initializeCamera() }
        }
    }

    /**
     *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
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
        val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
        val rotation = previewView.display.rotation

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        // Camera Selector
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            // Image Preview
            imagePreview = Preview.Builder().apply {
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(rotation)
            }.build()

            imagePreview.setSurfaceProvider(previewView.previewSurfaceProvider)

            // Image Analysis
            imageAnalysis = ImageAnalysis.Builder()
                // We request aspect ratio but no resolution
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)
                .build()
                .also {


                    it.setAnalyzer(executor, BarcodeAnalyzer({ qrCodes ->
                        qrCodes.forEach {
                            Log.d(TAG, "QR Code detected: ${it.rawValue}.")
                        }
                    }))
                }

            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll()

            try {
                // A variable number of use-cases can be passed here -
                // camera provides access to CameraControl & CameraInfo
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, imagePreview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

        close_button.setOnClickListener { onBackPressed() }
    }

    companion object {
        private const val TAG = "BarcodePluginActivity"
        const val OPTIONS_VALUE = "OptionsValue"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}
