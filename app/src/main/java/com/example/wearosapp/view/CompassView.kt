package com.example.wearosapp.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import com.example.wearosapp.R
import kotlin.math.abs

abstract class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    var currentAngle = 0F
    private var moveSpeed = 0.15F
    private var oldAngle = 0F
    private var rotationSpeed = 0.15F
    private var targetAngle = 0F
    private val drawHandler = Handler(Looper.getMainLooper())
    private lateinit var rotationTextView: TextView

    init {
        holder.addCallback(this)
    }

    @SuppressLint("SetTextI18n")
    private fun drawUi(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)
        canvas.save()
        pointNorth(canvas)
        drawContent(canvas)
        canvas.restore()

        // Mise à jour du texte de rotation
        rotationTextView.text = "${currentAngle.toInt()}°"
    }

    private fun pointNorth(canvas: Canvas) {
        val angleDiff = currentAngle - oldAngle
        val totalRotation = (angleDiff + 360) % 360
        if (totalRotation < 180) {
            oldAngle += totalRotation * moveSpeed
            oldAngle %= 360
        } else {
            oldAngle -= (360 - totalRotation) * moveSpeed
            if (oldAngle < 0) {
                oldAngle += 360
            }
        }

        canvas.rotate(-oldAngle, width / 2F, height / 2F)
    }

    private fun updateAngle() {
        if (currentAngle == targetAngle) return

        val diff = targetAngle - currentAngle
        val rotationAmount = rotationSpeed * diff

        if (abs(rotationAmount) < 0.1F) {
            currentAngle = targetAngle
        } else {
            currentAngle += rotationAmount
        }

        if (currentAngle < 0) {
            currentAngle += 360
        } else if (currentAngle >= 360) {
            currentAngle -= 360
        }
    }

    abstract fun drawContent(canvas: Canvas)
    abstract fun windowChangeAngle(width: Int, height: Int)

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        windowChangeAngle(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        drawHandler.removeCallbacksAndMessages(null)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        drawHandler.post { draw() }
        rotationTextView = (context as Activity).findViewById(R.id.rotationTextView)
    }

    private fun draw() {
        val canvas = holder.lockCanvas()
        if (canvas != null) {
            try {
                drawUi(canvas)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
        updateAngle()
        drawHandler.postDelayed({ draw() }, 5)
    }
}