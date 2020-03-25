package com.salesforce.barcodescannerplugin

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class BarcodeAnalyzer (private val onBarcodeDetected: (List<FirebaseVisionBarcode>) -> Unit, private val barcodeScannerOptions: BarcodeScannerOptions? = null): ImageAnalysis.Analyzer {
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

    override fun analyze(image: ImageProxy) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
            .setHeight(image.height)
            .setWidth(image.width)
            .setRotation(rotationDegreesToFirebaseRotation(image.imageInfo.rotationDegrees))
            .build()
        val firebaseImage = FirebaseVisionImage.fromByteBuffer(image.planes[0].buffer, metadata)
        detector.detectInImage(firebaseImage)
            .addOnSuccessListener(onBarcodeDetected)
            .addOnFailureListener { Log.e("BarcodeAnalyzer", "Fail", it) }

        image.close()
    }

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