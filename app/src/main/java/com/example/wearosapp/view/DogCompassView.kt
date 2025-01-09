package com.example.wearosapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.example.wearosapp.R
import com.example.wearosapp.expension.dp2px
import com.example.wearosapp.model.Dog

class DogCompassView : CompassView {

    private var pointNorthTriangleLength = 0F
    private var pointNorthTrianglePath = Path()
    private var pointerPath = Path()
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
    private var bitmapRect = Rect()
    private var dogs = emptyList<Dog>()


    private var paint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        strokeWidth = dp2px(2).toFloat()
        style = Paint.Style.STROKE
        textAlign = Paint.Align.CENTER
        textSize = dp2px(19).toFloat()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun drawContent(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#FFFFFF"))
        drawScale(canvas)
        drawPointNorthTriangle(canvas)
        drawPointerPath(canvas)
        drawCenterCircle(canvas)
    }

    private fun drawPointerPath(canvas: Canvas) {
        dogs.forEachIndexed { _ , dog ->
            canvas.save()
            canvas.rotate(dog.angle, cx, cy)
            val dogIconBitmap = dog.getDogIconBitmap(context)
            if (dogIconBitmap != null) {
                val dogIconWidth = dogIconBitmap.width
                val dogIconHeight = dogIconBitmap.height
                val iconLeft = cx - dogIconWidth / 2F
                val iconTop = pointNorthTriangleLength + selectHeight
                val iconRight = cx + dogIconWidth / 2F
                val iconBottom = iconTop + dogIconHeight

                canvas.drawBitmap(dogIconBitmap, null, RectF(iconLeft, iconTop, iconRight, iconBottom) , paint)
                if (dog.isSelected) {
                    val pointerLength = selectHeight + dogIconHeight + context.resources.getDimension(R.dimen.pointerLength)
                    val pointerColor = dog.getDominantColor(context)
                    paint.color = pointerColor
                    val pointerPath = Path()
                    pointerPath.moveTo(cx - 8F, cy)
                    pointerPath.lineTo(cx + 8F, cy)
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
                0F -> "N"
                90F -> "E"
                180F -> "S"
                270F -> "W"
                else -> ""
            }

            if (currentAngle % 30F == 0F) {
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
                    pointNorthTriangleLength + selectHeight / 5 * 2,
                    paint
                )
            }

            canvas.restore()
        }
    }

    override fun windowChangeAngle(width: Int, height: Int) {
        cx = width / 2F
        cy = height / 2F
        rayon = width.coerceAtMost(height).toFloat()
        circonference = (2 * Math.PI * rayon).toFloat()
        pointNorthTriangleLength = dp2px(22).toFloat()
        pointNorthTrianglePath.moveTo(cx, pointNorthTriangleLength / 5 * 1)
        pointNorthTrianglePath.lineTo(
            cx - pointNorthTriangleLength / 2,
            pointNorthTriangleLength / 5 * 4
        )
        pointNorthTrianglePath.lineTo(
            cx + pointNorthTriangleLength / 2,
            pointNorthTriangleLength / 5 * 4
        )

        selectSum = 360 / 3
        selectWidth = circonference / selectSum * 0.11F
        selectHeight = dp2px(18).toFloat()
        textX = cx
        textY = pointNorthTriangleLength + selectHeight + paint.textSize
        centerCircleR = dp2px(8).toFloat()

        if (dogs.isNotEmpty()) {
            val firstDog = dogs[0]
            val dogIconBitmap = firstDog.getDogIconBitmap(context)
            if (dogIconBitmap != null) {
                val dogIconWidth = dogIconBitmap.width
                val dogIconHeight = dogIconBitmap.height
                val iconTop = pointNorthTriangleLength + selectHeight
                val iconBottom = iconTop + dogIconHeight
                val iconLeft = cx - dogIconWidth / 2
                val iconRight = cx + dogIconWidth / 2
                bitmapRect.set(
                    iconLeft.toInt() ,
                    iconTop.toInt() ,
                    iconRight.toInt() ,
                    iconBottom.toInt()
                )
                pointerPath.moveTo(cx , iconBottom)
                pointerPath.lineTo(cx - dp2px(6) , cy)
                pointerPath.lineTo(cx + dp2px(6) , cy)
            }
        }
    }

    fun setDogPointer(dogs: List<Dog>) {
        this.dogs = dogs
    }
}