package org.kreal.hkshare.nettyShare.HttpFile

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

/**
 * Created by lthee on 2017/10/29.
 */
class ApkFileFactory(private val packageManager: PackageManager, private val root: String) : HttpFileFactory, HttpFile {
    override val isFile: Boolean
        get() = false
    override val etag: String
        get() = "teosksasdfasdf"
    override val name: String
        get() = "app"
    override val isDirectory: Boolean
        get() = true
    override val uri: String
        get() = root
    override val channel: FileChannel?
        get() = null

    override fun exist(): Boolean = true

    override fun listfile(): Array<HttpFile> {
        val files = mutableListOf<HttpFile>()
        for ((k, v) in appinfos) {
            files.add(ApkHttpFile(File(v), "$root/$k.apk", k))
        }
        return files.toTypedArray()
    }

    override fun getInputStream(pos: Long): InputStream {
        throw IOException()
    }

    override fun getmimetype(): String = ""

    override fun length(): Long = 0

    override fun lastModified(): Long = 0

    private var appinfos: Map<String, String>

    init {
        val ps = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)
        val infos = mutableMapOf<String, String>()
        ps.forEach {
            if ((it.flags % 2) != ApplicationInfo.FLAG_SYSTEM) {
                val path = it.sourceDir
                val lable = it.loadLabel(packageManager).toString()
                infos.put(lable, path)
            }
        }
        appinfos = infos
    }

    override fun newHttpFile(path: String): HttpFile {
        if (path == root || path == root + '/')
            return this
        val lable = path.replaceFirst(root + '/', "").replace(".apk", "")
        return ApkHttpFile(File(appinfos[lable]), path, lable)
    }
}