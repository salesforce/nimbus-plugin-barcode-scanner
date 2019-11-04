/*
 *
 * Copyright (c) 2019, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin.barcodedetection

import android.util.Log
import androidx.annotation.MainThread
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.salesforce.barcodescannerplugin.Utils
import com.salesforce.barcodescannerplugin.camera.CameraReticleAnimator
import com.salesforce.barcodescannerplugin.camera.FrameProcessorBase
import com.salesforce.barcodescannerplugin.camera.GraphicOverlay
import com.salesforce.barcodescannerplugin.camera.WorkflowModel
import com.salesforce.barcodescannerplugin.camera.WorkflowModel.WorkflowState
import java.io.IOException

/** A processor to run the barcode detector.  */
class BarcodeProcessor(graphicOverlay: GraphicOverlay, private val workflowModel: WorkflowModel, options: FirebaseVisionBarcodeDetectorOptions? = null) :
    FrameProcessorBase<List<FirebaseVisionBarcode>>() {

    private val detector =
        if (options == null) FirebaseVision.getInstance().visionBarcodeDetector else FirebaseVision.getInstance().getVisionBarcodeDetector(
            options
        )
    private val cameraReticleAnimator: CameraReticleAnimator = CameraReticleAnimator(graphicOverlay)

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionBarcode>> =
        detector.detectInImage(image)

    @MainThread
    override fun onSuccess(
        image: FirebaseVisionImage,
        results: List<FirebaseVisionBarcode>,
        graphicOverlay: GraphicOverlay
    ) {

        if (!workflowModel.isCameraLive) return

        Log.d(TAG, "Barcode result size: ${results.size}")

        // Picks the barcode, if exists, that covers the center of graphic overlay.

        val barcodeInCenter = results.firstOrNull { barcode ->
            val boundingBox = barcode.boundingBox ?: return@firstOrNull false
            val box = graphicOverlay.translateRect(boundingBox)
            box.contains(graphicOverlay.width / 2f, graphicOverlay.height / 2f)
        }

        graphicOverlay.clear()
        if (barcodeInCenter == null) {
            cameraReticleAnimator.start()
            graphicOverlay.add(BarcodeReticleGraphic(graphicOverlay, cameraReticleAnimator))
            workflowModel.setWorkflowState(WorkflowState.DETECTING)
        } else {
            cameraReticleAnimator.cancel()
            workflowModel.setWorkflowState(WorkflowState.DETECTED)
            workflowModel.detectedBarcode.setValue(barcodeInCenter)
        }
        graphicOverlay.invalidate()
    }

    override fun onFailure(e: Exception) {
        Utils.postError(TAG, "Barcode detection failed!", e)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Utils.postError(TAG, "Failed to close barcode detector!", e)
        }
    }

    companion object {
        private const val TAG = "BarcodeProcessor"
    }
}
