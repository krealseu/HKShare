package org.kreal.hkshare.NanoShare.Resource

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import fi.iki.elonen.NanoHTTPD
import org.kreal.hkshare.NanoShare.HttpFile.ApkHttpFile
import org.kreal.hkshare.NanoShare.HttpFile.HttpFile
import org.kreal.hkshare.extensions.getAppico
import org.kreal.hkshare.extensions.logi
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lthee on 2017/10/17.
 */
class ApkResource(private val packageManager: PackageManager, val path: String) : Resource(), HttpFile {
    override val channel: FileChannel
        get() = FileInputStream(File("adf")).channel
    override val isFile: Boolean = false
    override val etag: String = Integer.toHexString(this.javaClass.simpleName.hashCode())
    override val name: String = "APP"
    override val isDirectory: Boolean = true
    override val uri: String = path

    override fun exist(): Boolean = true
    override fun listfile(): Array<HttpFile> {
        val files = mutableListOf<HttpFile>()
        for ((k, v) in appinfos) {
            files.add(ApkHttpFile(File(v), uri + "/" + k + ".apk", k))
        }
        return files.toTypedArray()
    }

    override fun getInputStream(pos: Long): InputStream {
        throw Exception()
    }

    override fun getmimetype(): String = ""
    override fun length(): Long = 0
    override fun lastModified(): Long = 0

    override val matchs: Array<String> = arrayOf(path, path + "/[\\d\\D]*")

    lateinit private var appinfos: Map<String, String>

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

    override fun doGet(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        logi(session.uri)
        var uri = session.uri.trim()
        if (uri.indexOf('?') >= 0)
            uri = uri.substring(0, uri.indexOf('?'))
        if (uri.contains("../"))
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT,
                    "FORBIDDEN: Won't server ../ for security reasons.")
        var filename = uri.replaceFirst(path, "")

        if (filename == "" || filename == "/") {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, listDirectory(this))
        }
        filename = filename.substring(1, filename.length - 4)
        return serverfile(session, ApkHttpFile(File(appinfos[filename]), filename, uri))
    }

    fun listDirectory(file: HttpFile): String {
        val htmlBuilder = StringBuilder()
        htmlBuilder.append("<html><head>" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />" +
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>" +
                "<link href=\"/assets/appico/home.png\" rel=\"shortcut icon\"/>" +
                "<link href=\"/assets/css/itemstyle.css\" rel=\"stylesheet\" type=\"text/css\"/>" +
                "<title>" + file.name + "</title>" +
                "</head><body><h1>" + file.name + "</h1>")
        htmlBuilder.append("<div class=\"cc\">")
        var up: String? = null
        var path = file.uri
        if (path.length > 1) {
            if (path.endsWith('/'))
                path = path.substring(0, path.length - 1)
            val slash = path.lastIndexOf('/')
            up = when (slash) {
                0 -> "/"
                in 1..path.length -> path.substring(0, slash)
                else -> null
            }
        }
        if (up != null) {
            htmlBuilder.append(itemDiv(up, "/assets/appico/back.png", "..", "back", ""))
        }
        val gmtFrmt = SimpleDateFormat("MMM d,yyyy,HH:mm:ss", Locale.US)
        gmtFrmt.timeZone = TimeZone.getTimeZone("GTM+8")
        for (f in file.listfile()) {
            if (f.isDirectory) {
                htmlBuilder.append(itemDiv(f.uri, "/assets/appico/folder.png", f.name, "Directory", gmtFrmt.format(Date(f.lastModified()))))
            } else if (f.isFile) {
                val len = f.length()
                val size = when (len) {
                    in 0..1024 -> len.toString() + " bytes"
                    in 1024..1024 * 1024 -> (len / 1024).toString() + "." + (len % 1024 / 10 % 100) + " KB"
                    else -> (len / (1024 * 1024)).toString() + "." + (len % (1024 * 1024) / 10 % 100) + " MB"
                }
                htmlBuilder.append(itemDiv(f.uri, f.name.getAppico(), f.name, size, gmtFrmt.format(Date(f.lastModified()))))
            }
        }
        htmlBuilder.append("""
</div>
</body></html>
            """)
        return htmlBuilder.toString()
    }

    private fun itemDiv(uri: String, image: String, title: String, size: String, date: String): String {
        return """
<div class="item" onclick="javascript:location='$uri'">
<img src="$image"/>
<div class="title">$title</div>
<div class="size">$size</div>
<div class="date">$date</div>
<div class="line"></div>
</div>
            """
    }
}