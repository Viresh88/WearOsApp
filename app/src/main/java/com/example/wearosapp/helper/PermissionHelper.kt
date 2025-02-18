package com.example.wearosapp.helper

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class PermissionHelper(private val activity: Activity) {
    private val BLE_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
    )

    private val ANDROID_12_BLE_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun checkPermissions(): Boolean {
        val requestedPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            BLE_PERMISSIONS
        } else {
            ANDROID_12_BLE_PERMISSIONS
        }
        for (permission in requestedPermissions) {
            val permissionGranted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(requestedPermissions, LOCATION_PERMISSION_REQUEST_CODE)
                }
                return false
            }
        }
        return true
    }

    fun onRequestPermissionsResult(requestCode: Int , grantResults: IntArray): Boolean {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }
        return false
    }
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
}

