package id.co.mmksi.mitsubishimotors.base.util.permissionkit

import android.app.Activity
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference


// request perizinan untuk diminta ulang setelah ada message peringatannya

class PermissionRequest internal constructor(
    activity: Activity,
    val permissions: List<String>,
    private val requestCode: Int
) {

    private val weakActivity: WeakReference<Activity> = WeakReference(activity)


//     Invoke funnction ini setelah message peringatannya muncul.

    fun retry() {
        val activity = weakActivity.get()
        activity?.let { ActivityCompat.requestPermissions(it, permissions.toTypedArray(), requestCode) }
    }
}