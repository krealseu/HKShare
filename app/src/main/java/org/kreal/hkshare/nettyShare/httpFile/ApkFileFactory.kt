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
class ApkFileFactory( packageManager: PackageManager, private val httpPath: String) : HttpFileFactory, HttpFile {
    override val isFile: Boolean
        get() = false
    override val eTag: String
        get() = "  "
    override val name: String
        get() = "app"
    override val isDirectory: Boolean
        get() = true
    override val uri: String
        get() = httpPath
    override val channel: FileChannel?
        get() = null

    override fun exist(): Boolean = true

    override fun listFiles(): Array<HttpFile> {
        val files = mutableListOf<HttpFile>()
        for ((k, v) in appLists) {
            files.add(ApkHttpFile(File(v), "$httpPath/$k.apk", k))
        }
        return files.toTypedArray()
    }

    override fun getInputStream(pos: Long): InputStream {
        throw IOException()
    }

    override fun getMimeType(): String = ""

    override fun length(): Long = 0

    override fun lastModified(): Long = 0

    private val appLists: MutableMap<String, String> = HashMap()

    init {
        loadApp(packageManager)
    }

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

    override fun newHttpFile(path: String): HttpFile {
        if (path == httpPath || path == httpPath + '/')
            return this
        val lable = path.replaceFirst(httpPath + '/', "").replace(".apk", "")
        return ApkHttpFile(File(appLists[lable]), path, lable)
    }
}