package com.salesforce.barcodescannerplugin

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.camera.core.*
import androidx.camera.core.CameraSelector.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BarcodeScannerPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : PreviewView(context, attrs, defStyleAttr, defStyleRes), LifecycleEventObserver {

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var imageAnalysis: ImageAnalysis? = null

    private val cameraSelector = Builder().requireLensFacing(LENS_FACING_BACK).build()

    /**
     *  start barcode scanning
     *  @param lifecycleOwner the activity of this view. camera related status maintenance is hooked to it.
     *  @param analyzer the image analyzer
     */
    fun startScan(lifecycleOwner: LifecycleOwner, analyzer: ImageAnalysis.Analyzer) {

        enableTouchToFocusAndPinchToZoom()

        val metrics = DisplayMetrics().also { this.display.getRealMetrics(it) }
        val rotation = display.rotation

        val screenAspectRatio = getAspectRation(metrics.widthPixels, metrics.heightPixels)

        implementationMode = ImplementationMode.COMPATIBLE

        cameraProviderFuture = ProcessCameraProvider.getInstance(context).apply {
            addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()

                // Image Preview
                preview = Preview.Builder().apply {
                    setTargetAspectRatio(screenAspectRatio)
                    setTargetRotation(rotation)
                }.build()

                preview?.setSurfaceProvider(surfaceProvider)

                // Image Analysis
                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetAspectRatio(screenAspectRatio)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetRotation(rotation)
                    .build()
                    .apply { setAnalyzer(executor, analyzer) }

                // Must unbind the use-cases before rebinding them
                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            }, ContextCompat.getMainExecutor(context))
        }
    }

    /**
     * in some cases, when the light is not optimal, auto whole view metering and focus might not good
     * for do the scanning successful, so enable the capability that let the user to touch a point on the camera preview to
     * take that spot as metering spot ( auto focus, auto exposure, and auto white balance), it's very helpful
     * on the old device which auto metering is not advanced and fast.
     *
     * new devices has crazy zoom, like samsung s20 has 100x zoom. pinch zoom gives user capability to zoom in or out
     * properly to do the scanning without blindly rely on camera default behavior
     */
    private fun enableTouchToFocusAndPinchToZoom() {

        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val cameraInfo = camera?.cameraInfo
                val currentZoomRatio: Float = cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(context, listener)

        setOnTouchListener { _, event ->
            // pinch to zoom
            scaleGestureDetector.onTouchEvent(event)

            // touch to focus
            if (camera != null && event.action == MotionEvent.ACTION_DOWN) {
                camera?.cameraControl?.startFocusAndMetering(
                    FocusMeteringAction.Builder(
                        meteringPointFactory.createPoint(
                            event.x,
                            event.y
                        )
                    ).build()
                )
                return@setOnTouchListener true
            } else {
                return@setOnTouchListener false
            }
        }
    }

    private fun getAspectRation(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE))
            AspectRatio.RATIO_4_3
        else AspectRatio.RATIO_16_9
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == ON_DESTROY) {
            // do some cleanup when destroyed
            imageAnalysis?.clearAnalyzer()
            imageAnalysis = null
            camera = null
            executor.shutdown()
        }
    }
}