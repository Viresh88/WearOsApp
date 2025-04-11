package com.example.wearosapp.helper

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.wearosapp.R

private const val PREFS_NAME = "MyPrefs"
private const val KEY_DIALOG_SHOWN = "dialogShown"
class SensorHelper(private val activity: Activity) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometerSensor: Sensor? = null
    private var magneticSensor: Sensor? = null
    private var mGravity = FloatArray(3)
    private var mGeomagnetic = FloatArray(3)
    private var azimuth = 0f
    private var onSensorListener: OnSensorListener? = null

    init {
        sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magneticSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun register(onSensorListener: OnSensorListener) {
        this.onSensorListener = onSensorListener

        val hasMagneticSensor = magneticSensor != null
        val hasAccelerometerSensor = accelerometerSensor != null

        if (hasMagneticSensor && hasAccelerometerSensor) {
            sensorManager?.registerListener(
                this,
                magneticSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            sensorManager?.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        } else {
            showSensorWarningDialog()
        }
    }

    fun unregister() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.97f

        event.let {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0]
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1]
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2]
            }

            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0]
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1]
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2]
            }

            val rotationMatrix = FloatArray(9)
            val inclinationMatrix = FloatArray(9)
            val success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, mGravity, mGeomagnetic)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                azimuth = (azimuth + 360) % 360

                val trueNorthAzimuth = calculateTrueNorthAzimuth(azimuth)
                onSensorListener?.onAngle(trueNorthAzimuth)
            }
        }
    }

    private fun calculateTrueNorthAzimuth(magneticNorthAzimuth: Float): Float {
        return magneticNorthAzimuth
    }

    override fun onAccuracyChanged(sensor: Sensor , accuracy: Int) {}

    private fun showSensorWarningDialog() {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dialogShown = prefs.getBoolean(KEY_DIALOG_SHOWN, false)

        if (!dialogShown) {
            val alertDialog = AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.attention))
                .setMessage(activity.getString(R.string.message_sensor))
                .setPositiveButton(activity.getString(R.string.cancel)) { dialog: DialogInterface , _: Int ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .create()

            alertDialog.setOnShowListener {
                alertDialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bottom_style_dialog))

                val positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                val spannableString = SpannableString(positiveButton.text)
                spannableString.setSpan(ForegroundColorSpan(Color.RED) , 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                positiveButton.text = spannableString
            }

            alertDialog.show()

            val editor = prefs.edit()
            editor.putBoolean(KEY_DIALOG_SHOWN, true)
            editor.apply()
        }
    }

    interface OnSensorListener {
        fun onAngle(angle: Float)
    }
}