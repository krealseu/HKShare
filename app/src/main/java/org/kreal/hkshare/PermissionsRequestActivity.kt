package org.kreal.hkshare

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import org.kreal.hkshare.extensions.logi
import java.lang.StringBuilder

@TargetApi(Build.VERSION_CODES.M)
class PermissionsRequestActivity : Activity() {
    private lateinit var permissions: Array<String>
    private var isMust = true
    private val REQUEST_CODE = 423
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            permissions = getPermissions()
            isMust = intent.getBooleanExtra(NEED, true)
        } catch (e: Exception) {
            Log.e(TAG, "error intent")
            finish()
        }

        if (savedInstanceState == null) {
            if (hasStoragePermisson())
                return finish()
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isAllGrant(grantResults)) {
            setResult(0)
            return finish()
        }

        if (!isMust) {
            setResult(1)
            return finish()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                var dialog = AlertDialog.Builder(this)
                dialog.setTitle("以下权限是必须的")
                        .setMessage(permissions.toString())
                        .setNegativeButton("Cancel") { _, _ -> setResult(1); finish() }
                        .setPositiveButton("Setting") { _, _ ->
                            val storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                            requestPermissions(storagePermissions, REQUEST_CODE)
                        }
                        .setCancelable(false)
                dialog.show()
            } else {
                var dialog = AlertDialog.Builder(this)
                dialog.setTitle("权限被拒绝")
                        .setMessage("请到设置中勾选该权限:\r\n" + permissions.toString())
                        .setNegativeButton("Cancel") { _, _ -> setResult(1); finish() }
                        .setPositiveButton("Go Setting") { _, _ -> startAppSettings() }
                        .setCancelable(false)
                dialog.show()
            }
        }
    }

    private fun isAllGrant(grantResults: IntArray?): Boolean {
        if (grantResults == null)
            return true
        grantResults.forEach { i ->
            if (i != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    private fun isAllGrant(): Boolean {
        permissions.forEach { str ->
            if (baseContext.checkSelfPermission(str) == PackageManager.PERMISSION_DENIED)
                return false
        }
        return true
    }

    override fun finish() {
        overridePendingTransition(0, 0)
        super.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logi("jjjjjj")
        if (isAllGrant()) {
            setResult(0)
            return finish()
        }
        if (!isMust) {
            setResult(1)
            return finish()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                var dialog = AlertDialog.Builder(this)
                dialog.setTitle("以下权限是必须的")
                        .setMessage(permissions.toString())
                        .setNegativeButton("Cancel") { _, _ -> setResult(1); finish() }
                        .setPositiveButton("Setting") { _, _ ->
                            val storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                            requestPermissions(storagePermissions, REQUEST_CODE)
                        }
                        .setCancelable(false)
                dialog.show()
            } else {
                var dialog = AlertDialog.Builder(this)
                dialog.setTitle("权限被拒绝")
                        .setMessage("请到设置中勾选该权限:\r\n" + permissions.toString())
                        .setNegativeButton("Cancel") { _, _ -> setResult(1); finish() }
                        .setPositiveButton("Go Setting") { _, _ -> startAppSettings()}
                        .setCancelable(false)
                dialog.show()
            }
        }
    }

    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + packageName)
        ActivityCompat.startActivityForResult(this, intent, 424, null)
    }

    private fun hasStoragePermisson() = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    companion object {

        private val PERMISSIONS = "premissions"
        private val NEED = "must be granted"

        private val TAG = this::class.java.simpleName

        private fun Array<out String>?.toString(): String {
            val stringBuilder = StringBuilder()
            for (str in this!!)
                stringBuilder.append(str)
            return stringBuilder.toString()
        }

        fun RequestPermissionsIntent(content: Context, vararg permissions: String, isMust: Boolean = true): Intent {
            val intent = Intent(content, PermissionsRequestActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra(PERMISSIONS, permissions)
            intent.putExtra(NEED, isMust)
            return intent
        }

        private fun PermissionsRequestActivity.getPermissions() = intent.getStringArrayExtra(PERMISSIONS)
    }

}
