package com.example.wearosapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.core.content.ContextCompat
import androidx.wear.widget.BoxInsetLayout
import com.example.wearosapp.R
import com.example.wearosapp.expension.dp2px
import com.example.wearosapp.model.Dog
import kotlin.math.min

class DogCompassView : CompassView {
    private var pointNorthTriangleLength = 0F
    private var pointNorthTrianglePath = Path()
    private var pointerPath = Path()
    private var centerCircleR = 0F
    private var rayon = 0F
    private var cx = 0F
    private var cy = 0F
    private var dogs = emptyList<Dog>()

    // Scaled dimensions for Wear OS
    private var scaleMarkerLength = 0F
    private var scaleTextSize = 0F
    private var iconSize = 0F
    private var pointerLength = 0F

    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        strokeWidth = dp2px(2).toFloat() // Reduced stroke width for better appearance
        style = Paint.Style.STROKE
        textAlign = Paint.Align.CENTER
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        // Handle insets for round/square screens
        if (insets.isRound) {
            setPadding(
                resources.getDimensionPixelSize(R.dimen.wear_inset_padding),
                resources.getDimensionPixelSize(R.dimen.wear_inset_padding),
                resources.getDimensionPixelSize(R.dimen.wear_inset_padding),
                resources.getDimensionPixelSize(R.dimen.wear_inset_padding)
            )
        }
        return insets
    }

    override fun drawContent(canvas: Canvas) {
        // Clear background with system color
//        canvas.drawColor(ContextCompat.getColor(context, R.color.wear_background))

        // Draw components
        drawScale(canvas)
        drawPointNorthTriangle(canvas)
        drawDogPointers(canvas)
        drawCenterCircle(canvas)
    }

    private fun drawDogPointers(canvas: Canvas) {
        dogs.forEach { dog ->
            canvas.save()
            canvas.rotate(dog.angle, cx, cy)

            val dogIconBitmap = dog.getDogIconBitmap(context)
            dogIconBitmap?.let { bitmap ->
                // Scale icon size based on screen size
                val scaledSize = iconSize.toInt()
                val iconLeft = cx - scaledSize / 2
                val iconTop = pointNorthTriangleLength + scaleMarkerLength

                canvas.drawBitmap(
                    bitmap,
                    null,
                    RectF(
                        iconLeft,
                        iconTop,
                        iconLeft + scaledSize,
                        iconTop + scaledSize
                    ),
                    paint
                )

                if (dog.isSelected) {
                    drawSelectedPointer(canvas, dog, iconTop + scaledSize)
                }
            }
            canvas.restore()
        }
    }

    private fun drawSelectedPointer(canvas: Canvas, dog: Dog, startY: Float) {
        paint.apply {
            color = dog.getDominantColor(context)
            style = Paint.Style.FILL
        }

        val pointerPath = Path().apply {
            moveTo(cx - 6f, startY)
            lineTo(cx + 6f, startY)
            lineTo(cx, startY + pointerLength)
            close()
        }
        canvas.drawPath(pointerPath, paint)
        paint.style = Paint.Style.STROKE
    }

    private fun drawCenterCircle(canvas: Canvas) {
        paint.apply {
            color = Color.parseColor("#B6B6B6")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, centerCircleR, paint)
        paint.style = Paint.Style.STROKE
    }

    private fun drawPointNorthTriangle(canvas: Canvas) {
        paint.apply {
            color = Color.RED
            style = Paint.Style.FILL_AND_STROKE
        }
        canvas.drawPath(pointNorthTrianglePath, paint)
    }

    private fun drawScale(canvas: Canvas) {
        paint.textSize = scaleTextSize

        for (i in 0 until 360 step 3) {
            val currentAngle = i.toFloat()
            canvas.save()
            canvas.rotate(currentAngle, cx, cy)

            when {
                currentAngle % 90 == 0f -> drawCardinalDirection(canvas, currentAngle)
                currentAngle % 30 == 0f -> drawMajorTick(canvas)
                else -> drawMinorTick(canvas)
            }

            canvas.restore()
        }
    }

    private fun drawCardinalDirection(canvas: Canvas, angle: Float) {
        val direction = when (angle) {
            0f -> "N"
            90f -> "E"
            180f -> "S"
            270f -> "W"
            else -> ""
        }

        paint.apply {
            color = ContextCompat.getColor(context, R.color.light_orange)
            style = Paint.Style.FILL
        }

        canvas.drawText(
            direction,
            cx,
            pointNorthTriangleLength + scaleMarkerLength + scaleTextSize * 1.5f,
            paint
        )
        canvas.drawLine(cx, pointNorthTriangleLength, cx, pointNorthTriangleLength + scaleMarkerLength, paint)
    }

    private fun drawMajorTick(canvas: Canvas) {
        paint.color = ContextCompat.getColor(context, R.color.light_orange)
        canvas.drawLine(
            cx,
            pointNorthTriangleLength,
            cx,
            pointNorthTriangleLength + scaleMarkerLength,
            paint
        )
    }

    private fun drawMinorTick(canvas: Canvas) {
        paint.color = ContextCompat.getColor(context, R.color.grey_line_color)
        canvas.drawLine(
            cx,
            pointNorthTriangleLength,
            cx,
            pointNorthTriangleLength + scaleMarkerLength * 0.4f,
            paint
        )
    }

    override fun windowChangeAngle(width: Int, height: Int) {
        // Calculate dimensions based on screen size
        val minDimension = min(width, height).toFloat()
        cx = width / 2f
        cy = height / 2f
        rayon = minDimension / 2f

        // Scale UI elements based on screen size
        pointNorthTriangleLength = minDimension * 0.08f
        scaleMarkerLength = minDimension * 0.06f
        scaleTextSize = minDimension * 0.07f
        iconSize = minDimension * 0.12f
        pointerLength = minDimension * 0.08f
        centerCircleR = minDimension * 0.03f

        // Update north pointer path
        pointNorthTrianglePath.apply {
            reset()
            moveTo(cx, pointNorthTriangleLength * 0.2f)
            lineTo(cx - pointNorthTriangleLength * 0.5f, pointNorthTriangleLength * 0.8f)
            lineTo(cx + pointNorthTriangleLength * 0.5f, pointNorthTriangleLength * 0.8f)
            close()
        }
    }

    fun setDogPointer(dogs: List<Dog>) {
        this.dogs = dogs
        invalidate()
    }
}