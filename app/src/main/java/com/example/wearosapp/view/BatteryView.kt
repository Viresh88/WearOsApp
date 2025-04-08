package com.example.wearosapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.wearosapp.R


class BatteryView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var width = 0
    private var height = 0

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    var power = 0
        set(value) {
            field = value.coerceIn(0..100)
            invalidate()
        }

    init {
        paint.strokeWidth = width / 20f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = measuredWidth
        height = measuredHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawHorizontalBattery(canvas)
    }

    private fun drawHorizontalBattery(canvas: Canvas) {
        // Define the rectangle representing the battery body
        val contentRect = RectF(0F, 0F, width.toFloat() - paint.strokeWidth * 2, height.toFloat())
        val cornerRadius = 10F // Adjust this value for desired roundness

        paint.color = ContextCompat.getColor(context, R.color.grey_pastel)
        // Draw the battery body with rounded corners
        canvas.drawRoundRect(contentRect, cornerRadius, cornerRadius, paint)

        paint.style = Paint.Style.FILL

        val offset = (width - paint.strokeWidth * 3) * power / 100f
        // Define the rectangle representing the battery level
        val rectF = RectF(paint.strokeWidth, paint.strokeWidth, offset, height - paint.strokeWidth)

        // Set the color based on battery level
        paint.color = when {
            power <= 15 -> Color.RED// Darker green for low battery
            power <= 50 -> Color.parseColor("#00AA00") // Mid-level green
            else -> Color.GREEN // Full green
        }
        // Draw the battery level
        canvas.drawRect(rectF, paint)

        // Define the rectangle representing the battery head
        val batteryHeadRect = RectF(width - paint.strokeWidth * 2, height * 0.25f, width.toFloat(), height * 0.75f)
        paint.color = Color.BLACK
        // Draw the battery head
        canvas.drawRect(batteryHeadRect, paint)
    }
}

