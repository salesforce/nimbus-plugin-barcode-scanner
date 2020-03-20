package com.salesforce.barcodescannerplugin.barcodedetection

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.view.ViewCompat.getRotation
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.nio.ByteBuffer

class BarcodeAnalyzer (private val onBarcodeDetected: (List<FirebaseVisionBarcode>) -> Unit, private val options: FirebaseVisionBarcodeDetectorOptions? = null): ImageAnalysis.Analyzer {


    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        val detector =
            if (options == null) FirebaseVision.getInstance().visionBarcodeDetector else FirebaseVision.getInstance()
                .getVisionBarcodeDetector(
                    options
                )

//        val firebaseImage = FirebaseVisionImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)

        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
            .setHeight(image.height)
            .setWidth(image.width)
            .setRotation(image.imageInfo.rotationDegrees)
            .build()
        val firebaseImage = FirebaseVisionImage.fromByteBuffer(image.planes[0].buffer, metadata)
        detector.detectInImage(firebaseImage)
            .addOnSuccessListener(onBarcodeDetected)
            .addOnFailureListener {Log.e("BarcodeAnalyzer", "Fail", it)}

        image.close()

    }
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
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