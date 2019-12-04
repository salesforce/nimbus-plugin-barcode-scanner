/*
 *
 * Copyright (c) 2019, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.chip.Chip
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.salesforce.barcodescannerplugin.barcodedetection.BarcodeProcessor
import com.salesforce.barcodescannerplugin.barcodedetection.BarcodeScannedEvent
import com.salesforce.barcodescannerplugin.camera.CameraSource
import com.salesforce.barcodescannerplugin.camera.CameraSourcePreview
import com.salesforce.barcodescannerplugin.camera.GraphicOverlay
import com.salesforce.barcodescannerplugin.camera.WorkflowModel
import com.salesforce.barcodescannerplugin.camera.WorkflowModel.WorkflowState
import kotlinx.android.synthetic.main.top_action_bar_in_live_camera.*
import org.greenrobot.eventbus.EventBus
import java.io.IOException

/** Demonstrates the barcode scanning workflow using camera preview.  */
class LiveBarcodeScanningActivity : AppCompatActivity() {

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var promptChip: Chip? = null
    private var promptChipAnimator: AnimatorSet? = null
    private var workflowModel: WorkflowModel? = null
    private var currentWorkflowState: WorkflowState? = null
    private var barcodeDetectorOptions: FirebaseVisionBarcodeDetectorOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val barcodeScannerOptions = intent.extras?.getSerializable(OPTIONS_VALUE) as BarcodeScannerOptions?
        if (barcodeScannerOptions != null) {
            val combinedType = barcodeScannerOptions.barcodeTypes.fold(0) { sum, barcodeType -> sum or barcodeType.toVisionBarcodeType() }
            barcodeDetectorOptions = FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(combinedType).build()
        }

        setContentView(R.layout.activity_live_barcode)
        preview = findViewById(R.id.camera_preview)
        graphicOverlay = findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay).apply {
            cameraSource = CameraSource(this)
        }

        promptChip = findViewById(R.id.bottom_prompt_chip)
        promptChipAnimator =
            (AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter) as AnimatorSet).apply {
                setTarget(promptChip)
            }

        close_button.setOnClickListener { onBackPressed() }

        setUpWorkflowModel()
    }

    override fun onResume() {
        super.onResume()

        workflowModel?.markCameraFrozen()
        currentWorkflowState = WorkflowState.NOT_STARTED
        cameraSource?.setFrameProcessor(BarcodeProcessor(graphicOverlay!!, workflowModel!!, barcodeDetectorOptions))
        workflowModel?.setWorkflowState(WorkflowState.DETECTING)
    }

    override fun onPause() {
        super.onPause()
        currentWorkflowState = WorkflowState.NOT_STARTED
        stopCameraPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
        cameraSource = null
    }

    private fun startCameraPreview() {
        val workflowModel = this.workflowModel ?: return
        val cameraSource = this.cameraSource ?: return
        if (!workflowModel.isCameraLive) {
            try {
                workflowModel.markCameraLive()
                preview?.start(cameraSource)
            } catch (e: IOException) {
                cameraSource.release()
                this.cameraSource = null
                Utils.postError(TAG, "Failed to start camera preview!", e)
            }
        }
    }

    private fun stopCameraPreview() {
        val workflowModel = this.workflowModel ?: return
        if (workflowModel.isCameraLive) {
            workflowModel.markCameraFrozen()
            preview?.stop()
        }
    }

    private fun setUpWorkflowModel() {
        workflowModel = ViewModelProviders.of(this).get(WorkflowModel::class.java)

        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel!!.workflowState.observe(this, Observer { workflowState ->
            if (workflowState == null || currentWorkflowState == workflowState) {
                return@Observer
            }

            currentWorkflowState = workflowState
            Log.d(TAG, "Current workflow state: ${currentWorkflowState!!.name}")

            val wasPromptChipGone = promptChip?.visibility == View.GONE

            when (workflowState) {
                WorkflowState.DETECTING -> {
                    promptChip?.visibility = View.VISIBLE
                    promptChip?.setText(R.string.prompt_point_at_a_barcode)
                    startCameraPreview()
                }
                WorkflowState.DETECTED -> {
                    promptChip?.visibility = View.GONE
                    stopCameraPreview()
                }
                else -> promptChip?.visibility = View.GONE
            }

            val shouldPlayPromptChipEnteringAnimation = wasPromptChipGone && promptChip?.visibility == View.VISIBLE
            promptChipAnimator?.let {
                if (shouldPlayPromptChipEnteringAnimation && !it.isRunning) it.start()
            }
        })

        workflowModel?.detectedBarcode?.observe(this, Observer { barcode ->
            if (barcode != null) {
                EventBus.getDefault().postSticky(BarcodeScannedEvent(BarcodeScannerResult(BarcodeType.fromVisionBarcode(barcode.format), barcode.displayValue ?: "")))
                this.finish()
            }
        })
    }

    companion object {
        private const val TAG = "LiveBarcodeActivity"
        const val OPTIONS_VALUE = "OptionsValue"
    }
}
