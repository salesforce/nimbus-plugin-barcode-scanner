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
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
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
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.salesforce.barcodescannerplugin.barcodedetection.BarcodeAnalyzer
import com.salesforce.barcodescannerplugin.databinding.BarcodePluginActivityBinding
import java.util.concurrent.Executors

class BarcodePluginActivity : AppCompatActivity() {
    private lateinit var binding: BarcodePluginActivityBinding

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var imagePreview: Preview
    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private lateinit var imageAnalysis: ImageAnalysis
    private val executor = Executors.newSingleThreadExecutor()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.barcode_plugin_activity)
        binding.lifecycleOwner = this
        previewView = binding.previewView
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

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

    private fun initializeCamera(){
        imagePreview = Preview.Builder().apply {
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
            setTargetRotation(previewView.display.rotation)
        }.build()

        imagePreview.setSurfaceProvider(previewView.previewSurfaceProvider)
        imageAnalysis = ImageAnalysis.Builder().apply {
            setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        }.build()

        imageAnalysis.setAnalyzer(executor, BarcodeAnalyzer ({ qrCodes ->
            qrCodes.forEach {
                Log.d("MainActivity", "QR Code detected: ${it.rawValue}.")
            }}))

        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.bindToLifecycle(this, cameraSelector)
        }, ContextCompat.getMainExecutor(this))
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                imagePreview
//                imageAnalysis
            )
            cameraControl = camera.cameraControl
            cameraInfo = camera.cameraInfo
        }, ContextCompat.getMainExecutor(this))
    }

    companion object {
        private const val TAG = "BarcodePluginActivity"
        const val OPTIONS_VALUE = "OptionsValue"
    }
}
