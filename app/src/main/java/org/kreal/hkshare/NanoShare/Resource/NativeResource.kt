package org.kreal.hkshare.NanoShare.Resource

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.kreal.hkshare.NanoShare.HKHttpServive
import org.kreal.hkshare.NanoShare.HttpFile.HttpFile
import org.kreal.hkshare.NanoShare.HttpFile.NativeHttpFile
import org.kreal.hkshare.extensions.getAppico
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lthee on 2017/10/13.
 */
class NativeResource(root: File, private val path: String) : Resource() {

    override val matchs: Array<String> = arrayOf(path, (path + "[\\d\\D]*"))

    private val wwwRoot = NativeHttpFile(root, path)

    override fun doGet(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        Log.i("NativeResource", session.uri)
        val response = HKHttpServive.HKResponse()
        var uri = session.uri.trim()
        if (uri.indexOf('?') >= 0)
            uri = uri.substring(0, uri.indexOf('?'))
        if (uri.contains("../")) {
            response.sendErro(NanoHTTPD.Response.Status.FORBIDDEN, "FORBIDDEN: Won't server ../ for security reasons.")
            return response
        }
        val filename = uri.replaceFirst(path, "")
        var file = NativeHttpFile(wwwRoot, filename)
        if (!file.exist()) {
            response.sendErro(NanoHTTPD.Response.Status.NOT_FOUND, "Error 404,resource not fount ..")
            return response
        }
        if (file.isDirectory) {
            response.sendHtml(NanoHTTPD.Response.Status.OK, listDirectory(file))
            return response
        }
        file(session, response, file)
        return response
    }

    fun file(session: NanoHTTPD.IHTTPSession?, response: HKHttpServive.HKResponse, file: HttpFile) {
        if (!file.isFile)
            return response.sendErro(NanoHTTPD.Response.Status.NOT_FOUND, "Error 404,resource not fount ..")
        val requestHeader = session?.headers
        // deal with ETag
        val IfNoneMatch = requestHeader?.get("If-None-Match".toLowerCase())
        val fileETage = file.etag
        response.addHeader("ETag", fileETage)
        if (IfNoneMatch != null) {
            if (IfNoneMatch.contentEquals(fileETage)) {
                response.status = NanoHTTPD.Response.Status.NOT_MODIFIED
                return
            }
        }
        // detail range
        val range = requestHeader?.get("Range".toLowerCase())
        val fSize = file.length()
        val pos = LongArray(2)
        pos[0] = 0
        pos[1] = fSize - 1L
        if (range != null) {
            var isstandard = true
            //初始化Range的个参数，并判断是否正规
            if (range.matches("bytes=\\d*-\\d*$".toRegex())) {
                val tmp = range.replace("bytes=", "")
                val posStr = tmp.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                if (posStr.size == 1)
                    if (tmp.startsWith("-")) {
                        pos[0] = fSize - java.lang.Long.parseLong(posStr[0])
                        pos[1] = fSize - 1L
                    } else {
                        pos[0] = java.lang.Long.parseLong(posStr[0])
                        pos[1] = fSize - 1L
                    }
                else if (posStr.size.toLong() == 2L) {
                    for (i in posStr.indices)
                        pos[i] = java.lang.Long.parseLong(posStr[i])
                } else
                    pos[0] = -1L
                if (pos[0] < 0 || pos[0] > pos[1] || pos[1] > fSize)
                    isstandard = false
            } else {
                isstandard = false
            }
            //对是否正规进行判断
            if (!isstandard) {
                response.status = NanoHTTPD.Response.Status.RANGE_NOT_SATISFIABLE
                return
            }
            //deal with If Match
            val ifMatch = requestHeader?.get("If-Match".toLowerCase())
            if (ifMatch != null) {
                if (!ifMatch!!.matches(fileETage.toRegex())) {
                    response.status = NanoHTTPD.Response.Status.PRECONDITION_FAILED
                    return
                }
            }
            //detail with if Range
            val ifRange = requestHeader?.get("If-Range".toLowerCase())
            if (ifRange != null) {
                if (!ifRange!!.matches(fileETage.toRegex())) {
                    pos[0] = 0
                    pos[1] = fSize - 1L
                }
            }
            response.status = NanoHTTPD.Response.Status.PARTIAL_CONTENT
        } else {
            response.status = NanoHTTPD.Response.Status.OK
        }
        val contentRange = String.format("bytes %d-%d/%d", pos[0], pos[1], fSize)
        response.addHeader("Content-Range", contentRange)
        response.setContentLengthLong(pos[1] - pos[0] + 1L)

        //deal heard
        response.addHeader("Accept-Ranges", "bytes")
        response.addHeader("Content-Type", file.getmimetype())
        response.setDateHeader("Last-Modified", file.lastModified())

        //处理响应体
        val inputstream = file.getInputStream(pos[0])
        response.data = inputstream
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