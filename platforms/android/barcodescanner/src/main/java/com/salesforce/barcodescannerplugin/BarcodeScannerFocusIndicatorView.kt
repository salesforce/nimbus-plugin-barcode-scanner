/*
 *
 * Copyright (c) 2020, Salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 *
 */
package com.salesforce.barcodescannerplugin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class BarcodeScannerFocusIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val veilColor: Int = resources.getColor(R.color.veil)
    private val veilPaint = Paint().apply { color = veilColor }
    private val cornerRadius =
        resources.getDimensionPixelSize(R.dimen.focus_box_corner_radius).toFloat() +
        resources.getDimensionPixelSize(R.dimen.focus_box_stroke_width).toFloat()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {

            val top = 0f
            val left = 0f
            val right = this@BarcodeScannerFocusIndicatorView.width.toFloat()
            val bottom = this@BarcodeScannerFocusIndicatorView.height.toFloat()

            // draw top left corner
            drawPath(Path().apply {
                moveTo(left, top)
                lineTo(left, top + cornerRadius)
                arcTo(getArchBounds(cornerRadius, cornerRadius, cornerRadius), 180f, 90f)
                close()
            }, veilPaint)

            // draw top right corner
            drawPath(Path().apply {
                moveTo(right, top)
                lineTo(right - cornerRadius, top)
                arcTo(
                    getArchBounds(right - cornerRadius, top + cornerRadius, cornerRadius),
                    270f,
                    90f
                )
                close()
            }, veilPaint)

            // draw bottom left corner
            drawPath(Path().apply {
                moveTo(left, bottom)
                lineTo(left + cornerRadius, bottom)
                arcTo(
                    getArchBounds(left + cornerRadius, bottom - cornerRadius, cornerRadius),
                    90f,
                    90f
                )
                close()
            }, veilPaint)

            // draw bottom right corner
            drawPath(Path().apply {
                moveTo(right, bottom)
                lineTo(right, bottom - cornerRadius)
                arcTo(
                    getArchBounds(right - cornerRadius, bottom - cornerRadius, cornerRadius),
                    0f,
                    90f
                )
                close()
            }, veilPaint)

        }
    }


    private fun getArchBounds(centerX: Float, centerY: Float, radius: Float) =
        RectF(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius
        )
}