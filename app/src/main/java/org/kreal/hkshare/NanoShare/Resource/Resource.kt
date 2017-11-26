package org.kreal.hkshare.NanoShare.Resource

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.*
import org.kreal.hkshare.NanoShare.HKHttpServive
import org.kreal.hkshare.NanoShare.HttpFile.HttpFile
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lthee on 2017/10/9.
 */
abstract class Resource {

    abstract val matchs: Array<String>
//    abstract fun getMatch(): Array<String>

    fun serve(session: NanoHTTPD.IHTTPSession?): Response {
//        Log.i("resource", session?.uri)
//        Log.i("resource", session?.method?.name)
        session ?: return dealNullSession()
        return doGet(session)
    }

    abstract fun doGet(session: NanoHTTPD.IHTTPSession): Response

    fun doPost(session: NanoHTTPD.IHTTPSession): Response {
        return doGet(session)
    }

    fun dealNullSession(): Response {
        return NanoHTTPD.newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Error")
    }

    companion object {
        fun serverfile(session: IHTTPSession?, file: HttpFile): Response {
            if (!file.isFile)
                return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
                        "Error 404,resource not fount ..")
            val requestHeader = session?.headers
            val responseHeader = mutableMapOf<String, String>()
            val reponseStatus: Response.Status
            // deal with ETag
            val IfNoneMatch = requestHeader?.get("If-None-Match".toLowerCase())
            val fileETage = file.etag
            responseHeader.put("ETag", fileETage)
            if (IfNoneMatch != null) {
                if (IfNoneMatch.contentEquals(fileETage)) {
                    return newFixedLengthResponse(Response.Status.NOT_MODIFIED, file.getmimetype(), null)
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
                    return newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, file.getmimetype(), null)
                }
                //deal with If Match
                val ifMatch = requestHeader?.get("If-Match".toLowerCase())
                if (ifMatch != null) {
                    if (!ifMatch!!.matches(fileETage.toRegex())) {
                        return newFixedLengthResponse(Response.Status.PRECONDITION_FAILED, file.getmimetype(), null)
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
                reponseStatus = Response.Status.PARTIAL_CONTENT
            } else {
                reponseStatus = Response.Status.OK
            }
            val contentRange = String.format("bytes %d-%d/%d", pos[0], pos[1], fSize)
            responseHeader.put("Content-Range", contentRange)
            responseHeader.put("Content-Length", (pos[1] - pos[0] + 1L).toString())

            //deal heard
            responseHeader.put("Accept-Ranges", "bytes")
//                    responseHeader.put("Content-Type", file.getmimetype())
            val dateformat = SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US)
            dateformat.timeZone = TimeZone.getTimeZone("GMT")
            val date = dateformat.format(Date(file.lastModified()))
            responseHeader.put("Last-Modified", date)

            //处理响应体
            val inputstream = file.getInputStream(pos[0])
            val response = HKHttpServive.HKResponse(reponseStatus, file.getmimetype(), inputstream, pos[1] - pos[0] + 1L)

            for ((key, value) in responseHeader) {
                response.addHeader(key, value)
            }
            return response
        }
    }
}