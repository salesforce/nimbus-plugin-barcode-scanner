/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.salesforce.barcodescannerplugin.events.FailedScanEvent
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayOutputStream

/** Utility class to provide helper methods.  */
object Utils {

    private const val REQUIRED_PERMISSION = "android.permission.CAMERA"

    fun arePermissionsGranted(activity: Activity) =
        checkSelfPermission(activity, REQUIRED_PERMISSION) == PackageManager.PERMISSION_GRANTED

    fun requestPermissions(activity: Activity) = ActivityCompat.requestPermissions(
        activity,
        arrayOf(REQUIRED_PERMISSION), /* requestCode= */ 0
    )

    fun shouldShowRequestPermissionRationale(activity: Activity) =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, REQUIRED_PERMISSION)

    fun convertImageToBitmap(image: ImageProxy): Bitmap {
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            image.width,
            image.height,
            Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) },
            true
        )
    }

}

fun Rect.extendRectBy(by: Int) =
    Rect(this.left - by, this.top - by, this.right + by, this.bottom + by)

fun Rect.scaleRectBy(xRation: Float, yRation: Float) = Rect(
    (this.left * xRation).toInt(),
    (this.top * yRation).toInt(),
    (this.right * xRation).toInt(),
    (this.bottom * yRation).toInt()
)


