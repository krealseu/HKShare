package org.kreal.hkshare.nettyShare.httpFile

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

/**
 * Created by lthee on 2017/10/29.
 *
 */
class ApkFileFactory(packageManager: PackageManager, private val rootHttp: String) : HttpFileFactory, HttpFile {
    init {
        loadApp(packageManager)
        if (!rootHttp.endsWith('/'))
            throw Exception("The root Path must end with '/'")
    }

    override val isFile: Boolean = false
    override val eTag: String = "  "
    override val name: String = "app"
    override val isReadable: Boolean = true
    override val isDirectory: Boolean = true
    override val uri: String = rootHttp
    override val channel: FileChannel? = null
    override fun exist(): Boolean = true
    override fun listFiles(): Array<HttpFile> {
        val files = mutableListOf<HttpFile>()
        for ((k, v) in appLists) {
            files.add(ApkHttpFile(File(v), "$rootHttp$k.apk", k))
        }
        return files.toTypedArray()
    }

    override fun getInputStream(pos: Long): InputStream = throw IOException()
    override fun getMimeType(): String = ""
    override fun length(): Long = 0
    override fun lastModified(): Long = 0
    private val appLists: MutableMap<String, String> = HashMap()
    private fun loadApp(packageManager: PackageManager) {
        Thread {
            val ps = packageManager.getInstalledPackages(0)
            appLists.clear()
            ps.forEach {
                if (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM <= 0) {
                    val path = it.applicationInfo.sourceDir
                    val label = it.applicationInfo.loadLabel(packageManager).toString()
                    appLists[label] = path
                }
            }
        }.start()
    }

    override fun newHttpFile(path: String): HttpFile = when {
        path == "" -> this
        path.lastIndexOf('/') != -1 -> EmptyHttpFile(path)
        else -> {
            val label = path.removeSuffix(".apk")
            if (appLists.containsKey(label))
                ApkHttpFile(File(appLists[label]), "$rootHttp$path", label)
            else EmptyHttpFile(path, "$rootHttp$path")
        }
    }

}