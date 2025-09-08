package com.kamal.smartimagereducer.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object Utility {
    lateinit var REQUIRED_PERMISSIONS: Array<String>
    var alertDialog: AlertDialog? = null
    const val MULTIPLE_PERMISSIONS = 10

    fun permissionArray(): Array<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                // Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                REQUIRED_PERMISSIONS = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        } else {
            REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES
            )
        }
        return REQUIRED_PERMISSIONS

    }

    fun showSettingsDialog(context: Activity, dismis: DismissSetting? = null) {

        if (alertDialog != null) {
            if (!alertDialog?.isShowing!!) {
                alertPermission(context, dismis)
            }
        } else {
            alertPermission(context, dismis)
        }
    }

    fun alertPermission(context: Activity, dismis: DismissSetting? = null) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("\"Need Permissions\"")
        builder.setMessage("\"This app needs permission to use this feature. You can grant them in app settings.\"")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("GOTO SETTINGS") { dialogInterface, which ->
            alertDialog?.dismiss()
            alertDialog = null
            openSettings(context)
        }
        builder.setNegativeButton("Cancel") { dialogInterface, which ->
            alertDialog?.dismiss()
            alertDialog = null
            if (dismis != null) {
                dismis.alertSetting()
            }


        }
        alertDialog = builder.create()
        alertDialog?.setCancelable(false)
        alertDialog!!.show()
    }

    fun openSettings(context: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivityForResult(intent, 101)
    }
}