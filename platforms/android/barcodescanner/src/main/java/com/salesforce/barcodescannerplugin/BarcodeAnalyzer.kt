/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * The barcode analyzer which takes a image proxy, call firebase ml vision to get the barcode.
 *
 * @param activity the analyzer run in a activity. Analyzing will stop automatically when
 *      activity become invisible, instead of some time crashing on some devices.
 * @param onBarcodeDetected callback to call when analyzer found a barcode
 * @param onBarcodeDetectFailed callback to call when analyzer failed
 * @param barcodeScannerOptions
 */
class BarcodeAnalyzer(
    private val activity: AppCompatActivity,
    private val onBarcodeDetected: (List<FirebaseVisionBarcode>, ImageProxy) -> Unit,
    private val onBarcodeDetectFailed: (Exception) -> Unit,
    private val barcodeScannerOptions: BarcodeScannerOptions? = null
) : ImageAnalysis.Analyzer, LifecycleEventObserver {

    init {
        activity.lifecycle.addObserver(this)
    }

    var isPaused: Boolean = false
    private var lastAnalyzedTimestamp = 0L

    private val detector: FirebaseVisionBarcodeDetector by lazy {
        if (barcodeScannerOptions == null) {
            FirebaseVision.getInstance().visionBarcodeDetector
        } else {
            val combinedType =
                barcodeScannerOptions.barcodeTypes.fold(0) { sum, barcodeType -> sum or barcodeType.toVisionBarcodeType() }
            val options =
                FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(combinedType)
                    .build()
            FirebaseVision.getInstance()
                .getVisionBarcodeDetector(
                    options
                )
        }
    }

    /**
     * skip analyzing if isPaused
     * @param image the ImageProxy to analyze
     */
    override fun analyze(image: ImageProxy) =
        if (isPaused) closeImageProxy(image) else doAnalyze(image)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            isPaused = false
        }
    }

    /**
     * do barcode detection on the image using firebase vision. one image a time,
     * next detection will come in only when current finishes, controlled by calling ImageProxy.close()
     *
     * @param image the image to analyze on
     */
    private fun doAnalyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >=
            TimeUnit.SECONDS.toMillis(1)
        ) {
            val metadata = FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                .setHeight(image.height)
                .setWidth(image.width)
                .setRotation(rotationDegreesToFirebaseRotation(image.imageInfo.rotationDegrees))
                .build()
            val firebaseImage =
                FirebaseVisionImage.fromByteBuffer(image.planes[0].buffer, metadata)
            detector.detectInImage(firebaseImage)
                .addOnSuccessListener(activity) {
                    // call callback if not paused
                    if (!isPaused) onBarcodeDetected(it, image)
                    closeImageProxy(image)
                }
                .addOnFailureListener(activity) {
                    // call callback if not paused
                    if (!isPaused) {
                        onBarcodeDetectFailed(it)
                    }
                    closeImageProxy(image)
                }
        }
    }

    /**
     * close the image proxy, so next image will be pushed down from camera preview for analyzing
     * @param image the image to close
     */
    private fun closeImageProxy(image: ImageProxy) = image.close()

    private fun rotationDegreesToFirebaseRotation(rotationDegrees: Int): Int {
        return when (rotationDegrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw IllegalArgumentException("Not supported")
        }
    }
}
