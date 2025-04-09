package com.example.wearosapp.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class CustomCompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f  // Increased text size for clarity
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val dogPointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }

    // Angle from sensor reading (current rotation of the compass)
    private var rotationAngle = 0f

    private var dogPointers: List<DogPointer> = emptyList()

    data class DogPointer(val angle: Float)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 2.5f

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(centerX, centerY, radius, paint)

        paint.style = Paint.Style.FILL
        val directions = listOf("N", "E", "S", "W")
        val angles = listOf(0, 90, 180, 270)
        directions.zip(angles).forEach { (dir, angle) ->
            val radian = Math.toRadians(angle.toDouble() - rotationAngle)
            val x = (centerX + radius * 0.8 * cos(radian)).toFloat()
            val y = (centerY + radius * 0.8 * sin(radian)).toFloat()
            canvas.drawText(dir, x, y, paint)
        }

        val arrowX = (centerX + radius * 0.7 * cos(Math.toRadians(-rotationAngle.toDouble()))).toFloat()
        val arrowY = (centerY + radius * 0.7 * sin(Math.toRadians(-rotationAngle.toDouble()))).toFloat()
        canvas.drawCircle(arrowX, arrowY, 10f, arrowPaint)

        dogPointers.forEach { dog ->
            val dogX = (centerX + radius * 0.6 * cos(Math.toRadians(-dog.angle.toDouble()))).toFloat()
            val dogY = (centerY + radius * 0.6 * sin(Math.toRadians(-dog.angle.toDouble()))).toFloat()
            canvas.drawCircle(dogX, dogY, 8f, dogPointerPaint)
        }
    }

    fun updateRotation(angle: Float) {
        rotationAngle = angle
        invalidate()
    }

    fun setDogPointer(pointers: List<DogPointer>) {
        dogPointers = pointers
        invalidate()
    }
}
