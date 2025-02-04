package com.example.wearosapp.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin


class CustomCompassView@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 5f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private var rotationAngle = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 2.5f

        // Draw Compass Circle
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Draw Direction Labels
        paint.style = Paint.Style.FILL
        val directions = listOf("N", "E", "S", "W")
        val angles = listOf(0, 90, 180, 270)

        directions.zip(angles).forEach { (dir, angle) ->
            val radian = Math.toRadians(angle.toDouble() - rotationAngle)
            val x = (centerX + radius * 0.8 * cos(radian)).toFloat()
            val y = (centerY + radius * 0.8 * sin(radian)).toFloat()
            canvas.drawText(dir, x, y, paint)
        }

        // Draw Arrow
        val arrowX = (centerX + radius * 0.7 * cos(Math.toRadians(-rotationAngle.toDouble()))).toFloat()
        val arrowY = (centerY + radius * 0.7 * sin(Math.toRadians(-rotationAngle.toDouble()))).toFloat()
        canvas.drawCircle(arrowX, arrowY, 10f, arrowPaint)
    }

    fun updateRotation(angle: Float) {
        rotationAngle = angle
        invalidate()
    }
}

