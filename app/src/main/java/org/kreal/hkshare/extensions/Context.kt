package org.kreal.hkshare.extensions

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import org.kreal.hkshare.HKService
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

/**
 * Created by lthee on 2017/10/14.
 */
infix fun Context.checkService(name: String): Boolean {
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runService = manager.getRunningServiceControlPanel(ComponentName(
            this, HKService::class.java
    ))
    val m = manager.runningAppProcesses
    for (v in m) {
        Log.i("Service", v.toString())
    }
    return runService == null
}
