/*
 *
 * Copyright (c) 2019, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */

package com.salesforce.barcodescannerplugin.barcodedetection

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Paint.Style
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.salesforce.barcodescannerplugin.R
import com.salesforce.barcodescannerplugin.camera.GraphicOverlay

internal abstract class BarcodeGraphicBase(overlay: GraphicOverlay) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.barcode_reticle_stroke)
        style = Style.STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(R.dimen.barcode_reticle_stroke_width).toFloat()
    }

    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.barcode_reticle_background)
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = boxPaint.strokeWidth
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    val boxCornerRadius: Float =
        context.resources.getDimensionPixelOffset(R.dimen.barcode_reticle_corner_radius).toFloat()

    val boxRect: RectF = getBarcodeReticleBox(overlay)

    override fun draw(canvas: Canvas) {
        // Draws the dark background scrim and leaves the box area clear.
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), scrimPaint)
        // As the stroke is always centered, so erase twice with FILL and STROKE respectively to clear
        // all area that the box rect would occupy.
        eraserPaint.style = Style.FILL
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        eraserPaint.style = Style.STROKE
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        // Draws the box.
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, boxPaint)
    }

    private fun getBarcodeReticleBox(overlay: GraphicOverlay): RectF {
        val overlayWidth = overlay.width.toFloat()
        val overlayHeight = overlay.height.toFloat()
        val boxWidth = overlayWidth * 80 / 100
        val boxHeight = overlayHeight * 35 / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        return RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2)
    }
}
