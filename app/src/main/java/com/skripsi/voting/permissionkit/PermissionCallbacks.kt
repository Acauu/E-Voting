package com.skripsi.voting.permissionkit

import id.co.mmksi.mitsubishimotors.base.util.permissionkit.PermissionRequest


interface PermissionCallbacks {


//    di-invoke jika semua perizinan diberikan.

    fun onGranted()


//     di-invoke jika semua perizinan ditolak.
//
//     @param permissions list of permissions user ditolak

    fun onDenied(permissions: List<String>)


//     di-invoke jika message peringatan muncul.
//     @param permissionRequest digunakan untuk ulang permission request

    fun onShowRationale(permissionRequest: PermissionRequest)


//     di-invoke jika semua perizinan ditolak benar - benar.
//     @param permissions list of permissions user akan pakai fungsi on NeverAskAgain

    fun onNeverAskAgain(permissions: List<String>)
}

/**
 * DSL implementation for [PermissionCallbacks].
 */
class PermissionCallbacksDSL : PermissionCallbacks {

    private var onGranted: () -> Unit = {}
    private var onDenied: (permissions: List<String>) -> Unit = {}
    private var onShowRationale: (permissionRequest: PermissionRequest) -> Unit = {}
    private var onNeverAskAgain: (permissions: List<String>) -> Unit = {}

    fun onGranted(func: () -> Unit) {
        onGranted = func
    }

    fun onDenied(func: (permissions: List<String>) -> Unit) {
        onDenied = func
    }

    fun onShowRationale(func: (permissionRequest: PermissionRequest) -> Unit) {
        onShowRationale = func
    }

    fun onNeverAskAgain(func: (permissions: List<String>) -> Unit) {
        onNeverAskAgain = func
    }

    override fun onGranted() {
        onGranted.invoke()
    }

    override fun onDenied(permissions: List<String>) {
        onDenied.invoke(permissions)
    }

    override fun onShowRationale(permissionRequest: PermissionRequest) {
        onShowRationale.invoke(permissionRequest)
    }

    override fun onNeverAskAgain(permissions: List<String>) {
        onNeverAskAgain.invoke(permissions)
    }

}