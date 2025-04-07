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

    // Paint for drawing the compass circle and direction labels.
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f  // Increased text size for clarity
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    // Paint for drawing the main sensor arrow.
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    // Paint for drawing dog pointers.
    private val dogPointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }

    // Angle from sensor reading (current rotation of the compass)
    private var rotationAngle = 0f

    // List of dog pointers. Each dog's angle is used to position its pointer.
    private var dogPointers: List<DogPointer> = emptyList()

    // Data class representing a dog pointer (you may adjust this structure to suit your Dog model)
    data class DogPointer(val angle: Float)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 2.5f

        // Draw the outer compass circle.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Draw direction labels (N, E, S, W)
        paint.style = Paint.Style.FILL
        val directions = listOf("N", "E", "S", "W")
        val angles = listOf(0, 90, 180, 270)
        directions.zip(angles).forEach { (dir, angle) ->
            val radian = Math.toRadians(angle.toDouble() - rotationAngle)
            val x = (centerX + radius * 0.8 * cos(radian)).toFloat()
            val y = (centerY + radius * 0.8 * sin(radian)).toFloat()
            canvas.drawText(dir, x, y, paint)
        }

        // Draw the main sensor arrow.
        val arrowX = (centerX + radius * 0.7 * cos(Math.toRadians(-rotationAngle.toDouble()))).toFloat()
        val arrowY = (centerY + radius * 0.7 * sin(Math.toRadians(-rotationAngle.toDouble()))).toFloat()
        canvas.drawCircle(arrowX, arrowY, 10f, arrowPaint)

        // Draw dog pointers. They are drawn at 60% of the radius.
        dogPointers.forEach { dog ->
            val dogX = (centerX + radius * 0.6 * cos(Math.toRadians(-dog.angle.toDouble()))).toFloat()
            val dogY = (centerY + radius * 0.6 * sin(Math.toRadians(-dog.angle.toDouble()))).toFloat()
            canvas.drawCircle(dogX, dogY, 8f, dogPointerPaint)
        }
    }

    /**
     * Updates the sensor rotation and forces a redraw.
     */
    fun updateRotation(angle: Float) {
        rotationAngle = angle
        invalidate()
    }

    fun setDogPointer(pointers: List<DogPointer>) {
        dogPointers = pointers
        invalidate()
    }
}
