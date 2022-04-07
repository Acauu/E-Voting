package com.skripsi.voting.permissionkit

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.skripsi.voting.permissionkit.PermissionsMap
import id.co.mmksi.mitsubishimotors.base.util.permissionkit.PermissionRequest



// perizinan kamera and invoke fungsi callback.
// @param permissions manifest permission list (contohnya[android.Manifest.permission.CAMERA])
// @param callbacks permission callback DSL


fun Activity.askPermissions(vararg permissions: String, callbacks: PermissionCallbacksDSL.() -> Unit) {

    val permissionCallbacks: PermissionCallbacks = PermissionCallbacksDSL().apply { callbacks() }

    val permissionsNeeded = permissions.filter { !isPermissionGranted(it) }

    if (permissionsNeeded.isNotEmpty()) {

        val shouldShowRationalePermissions = mutableListOf<String>()
        val shouldNotShowRationalePermissions = mutableListOf<String>()

        permissionsNeeded.forEach {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                shouldShowRationalePermissions.add(it)
            } else {
                shouldNotShowRationalePermissions.add(it)
            }
        }

        val requestCode = PermissionsMap.put(permissionCallbacks)

        if (shouldShowRationalePermissions.isNotEmpty()) {

            permissionCallbacks.onShowRationale(PermissionRequest(this, shouldShowRationalePermissions, requestCode))
            return
        }

        if (shouldNotShowRationalePermissions.isNotEmpty()) {

            ActivityCompat.requestPermissions(
                this,
                shouldNotShowRationalePermissions.toTypedArray(), requestCode
            )
        }

    } else {
        permissionCallbacks.onGranted()
    }
}



// Fungsi untuk mengatur hasil perizinanya
// Invoke dari fungsi [Activity.onRequestPermissionsResult]

fun Activity.handlePermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {

    val callbacks = PermissionsMap.get(requestCode)

    var allGranted = true
    val neverAskAgainPermissions = mutableListOf<String>()
    val deniedPermissions = arrayListOf<String>()

    permissions.forEachIndexed { index, permission ->
        if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
            allGranted = false
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                neverAskAgainPermissions.add(permission)
            } else {
                deniedPermissions.add(permission)
            }
        }
    }

    if (allGranted) {
        callbacks?.onGranted()
    } else {
        if (deniedPermissions.isNotEmpty()) {
            callbacks?.onDenied(deniedPermissions)
        }
        if (neverAskAgainPermissions.isNotEmpty()) {
            callbacks?.onNeverAskAgain(neverAskAgainPermissions)
        }
    }

}


// cek perizinanya diberikan tidak.
// @param permission Manifest permission (contoh [android.Manifest.permission.CAMERA])
// @return true kalau diberikan izin.

fun Activity.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}