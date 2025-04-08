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
import com.example.wearosapp.R
import com.example.wearosapp.expension.dp2px
import com.example.wearosapp.model.Dog
import kotlin.math.min

class DogCompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : CompassView(context, attrs) {

    private var pointNorthTriangleLength = 0F
    private val pointNorthTrianglePath = Path()
    private val pointerPath = Path()
    private var selectHeight = 0F
    private var selectWidth = 0F
    private var selectSum = 0
    private var centerCircleR = 0F
    private var rayon = 0F
    private var cx = 0F
    private var cy = 0F
    private var circonference = 0F
    private var textX = 0F
    private var textY = 0F
    private val bitmapRect = Rect()
    private var dogs = emptyList<Dog>()

    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        strokeWidth = dp2px(2).toFloat()
        style = Paint.Style.STROKE
        textAlign = Paint.Align.CENTER
        textSize = dp2px(10).toFloat()
    }

    // Handle window insets to support round screens on Wear OS.
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (insets.isRound) {
            setPadding(
                resources.getDimensionPixelSize(R.dimen.wear_inset_padding),
                resources.getDimensionPixelSize(R.dimen.wear_inset_padding),
                resources.getDimensionPixelSize(R.dimen.wear_inset_padding),
                resources.getDimensionPixelSize(R.dimen.wear_inset_padding)
            )
        }
        return super.onApplyWindowInsets(insets)
    }

    override fun drawContent(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#FFFFFF"))
        drawScale(canvas)
        drawPointNorthTriangle(canvas)
        drawPointerPath(canvas)
        drawCenterCircle(canvas)
    }

    private fun drawPointerPath(canvas: Canvas) {
        dogs.forEach { dog ->
            canvas.save()
            canvas.rotate(dog.angle, cx, cy)
            val dogIconBitmap = dog.getDogIconBitmap(context)
            if (dogIconBitmap != null) {
                val dogIconWidth = dogIconBitmap.width
                val dogIconHeight = dogIconBitmap.height
                val iconLeft = cx - dogIconWidth / 2f
                val iconTop = pointNorthTriangleLength + selectHeight
                val iconRight = cx + dogIconWidth / 2f
                val iconBottom = iconTop + dogIconHeight

                canvas.drawBitmap(dogIconBitmap, null,
                    RectF(iconLeft, iconTop, iconRight, iconBottom), paint)

                if (dog.isSelected) {
                    val pointerLength = selectHeight + dogIconHeight +
                            context.resources.getDimension(R.dimen.pointerLength)
                    val pointerColor = dog.getDominantColor(context)
                    paint.color = pointerColor

                    // Reuse the class-level pointerPath
                    pointerPath.reset()
                    pointerPath.moveTo(cx - 8f, cy)
                    pointerPath.lineTo(cx + 8f, cy)
                    pointerPath.lineTo(cx, cy - pointerLength)
                    pointerPath.close()
                    canvas.drawPath(pointerPath, paint)
                }
            }
            canvas.restore()
        }
    }

    private fun drawCenterCircle(canvas: Canvas) {
        paint.color = Color.parseColor("#B6B6B6")
        canvas.drawCircle(cx, cy, centerCircleR, paint)
    }

    private fun drawPointNorthTriangle(canvas: Canvas) {
        paint.strokeWidth = selectWidth
        paint.color = Color.RED
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawPath(pointNorthTrianglePath, paint)
    }

    private fun drawScale(canvas: Canvas) {
        for (i in 0 until selectSum) {
            val currentAngle = (i * (360 / selectSum)).toFloat()
            paint.color = ContextCompat.getColor(context, R.color.light_orange)
            canvas.save()
            canvas.rotate(currentAngle, cx, cy)

            val text = when (currentAngle) {
                0f -> "N"
                90f -> "E"
                180f -> "S"
                270f -> "W"
                else -> ""
            }

            if (currentAngle % 30f == 0f) {
                canvas.drawText(text, textX, textY, paint)
                canvas.drawLine(
                    cx,
                    pointNorthTriangleLength,
                    cx,
                    pointNorthTriangleLength + selectHeight,
                    paint
                )
            } else {
                paint.color = ContextCompat.getColor(context, R.color.grey_line_color)
                canvas.drawLine(
                    cx,
                    pointNorthTriangleLength,
                    cx,
                    pointNorthTriangleLength + selectHeight * 0.4f,
                    paint
                )
            }
            canvas.restore()
        }
    }

    // This function is called when the view size or orientation changes.
    override fun windowChangeAngle(width: Int, height: Int) {
        cx = width / 2F
        cy = height / 2F

        // Use a fraction of the minimum dimension to define your main radius.
        val scaleFactor = 0.9f
        rayon = min(width, height).toFloat() * scaleFactor
        circonference = (2 * Math.PI * rayon).toFloat()

        // Increase dimensions proportionally
        pointNorthTriangleLength = dp2px(14).toFloat() * scaleFactor
        pointNorthTrianglePath.reset()
        pointNorthTrianglePath.moveTo(cx, pointNorthTriangleLength / 5f)
        pointNorthTrianglePath.lineTo(cx - pointNorthTriangleLength / 2f, pointNorthTriangleLength * 4f / 5f)
        pointNorthTrianglePath.lineTo(cx + pointNorthTriangleLength / 2f, pointNorthTriangleLength * 4f / 5f)
        pointNorthTrianglePath.close()

        selectSum = 360 / 3  // or another value if needed
        selectWidth = circonference / selectSum * 0.11f
        selectHeight = dp2px(18).toFloat() * scaleFactor
        textX = cx
        textY = pointNorthTriangleLength + selectHeight + paint.textSize
        centerCircleR = dp2px(8).toFloat() * scaleFactor

        // Optionally update the pointerPath base if needed.
        if (dogs.isNotEmpty()) {
            dogs.forEach { dog ->
                val existingDog = this.dogs.find { it.imei == dog.imei }
                if (existingDog != null) {
                    dog.isSelected = existingDog.isSelected
                    dog.levelSanction = existingDog.levelSanction
                }
            }
            dogs.find { it.isSelected }?.let { firstDog ->
                val dogIconBitmap = firstDog.getDogIconBitmap(context)
                dogIconBitmap?.let {
                    val dogIconWidth = it.width
                    val dogIconHeight = it.height
                    val iconTop = pointNorthTriangleLength + selectHeight
                    val iconBottom = iconTop + dogIconHeight
                    val iconLeft = cx - dogIconWidth / 2f
                    val iconRight = cx + dogIconWidth / 2f
                    bitmapRect.set(
                        iconLeft.toInt(),
                        iconTop.toInt(),
                        iconRight.toInt(),
                        iconBottom.toInt()
                    )
                    pointerPath.reset()
                    pointerPath.moveTo(cx, iconBottom.toFloat())
                    pointerPath.lineTo(cx - dp2px(6).toFloat(), cy)
                    pointerPath.lineTo(cx + dp2px(6).toFloat(), cy)
                    pointerPath.close()
                }
            }
        }
    }

    // Set updated dog data along with their calculated angles, then force a redraw.
    fun setDogPointer(dogs: List<Dog>) {
        this.dogs = dogs
        invalidate()
    }
}