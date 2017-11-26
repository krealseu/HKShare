package org.kreal.hkshare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat

/**
 * Created by lthee on 2017/11/19.
 */

class PermissionGranter(vararg permissions: String) {
    private val permissions: Array<Pair<String, Int>> = Array(permissions.size) { i ->
        permissions[i] to PackageManager.PERMISSION_DENIED
    }

    fun checkSelfPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true
        permissions.forEachIndexed { index, pair ->
            permissions[index] = pair.first to context.checkSelfPermission(pair.first)
        }
        return permissions.find { pair -> pair.second == PackageManager.PERMISSION_DENIED } == null
    }

    fun requestPermission(context: Activity) {
        if (checkSelfPermission((context)))
            return
        val intent = Intent(context, PermissionsRequestActivity::class.java)
        val ps: ArrayList<String> = arrayListOf()
        permissions.forEach { pair ->
            if (pair.second != PackageManager.PERMISSION_GRANTED) ps.add(pair.first)
        }
        intent.putExtra("pre", ps.toTypedArray())
        context.startActivity(intent)
    }

    fun resquestPermissionForResult(context: Activity, requestCode: Int, option: Bundle? = null) {
        if (checkSelfPermission((context)))
            return
        val intent = Intent(context, PermissionsRequestActivity::class.java)
        val ps: ArrayList<String> = arrayListOf()
        permissions.forEach { pair ->
            if (pair.second != PackageManager.PERMISSION_GRANTED) ps.add(pair.first)
        }
        intent.putExtra("pre", ps.toTypedArray())
        ActivityCompat.startActivityForResult(context, intent, requestCode, option)
    }
}