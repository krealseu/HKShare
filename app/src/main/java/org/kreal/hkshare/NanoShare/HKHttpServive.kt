package org.kreal.hkshare.NanoShare

import fi.iki.elonen.NanoHTTPD
import org.kreal.hkshare.NanoShare.Resource.DefaultResource
import org.kreal.hkshare.NanoShare.Resource.Resource
import org.kreal.hkshare.extensions.logi
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * Created by lthee on 2017/10/9.
 */
class HKHttpServive(hostname: String?, port: Int) : NanoHTTPD(hostname, port) {

    constructor(port: Int) : this(null, port)

    init {
        setAsyncRunner(HKAsyncRunner())
    }

    private var rountmap1: SortedMap<String, Resource> = sortedMapOf()

    private var rountmap2: SortedMap<String, Resource> = sortedMapOf()

    fun route(rout: String, rs: Resource) {
        if (rout.endsWith("/*"))
            rountmap2.put(rout, rs)
        else rountmap1.put(rout, rs)
        rountmap1 = rountmap1.toSortedMap(kotlin.Comparator { t1, t2 -> if (t1.length == t2.length) t1.compareTo(t2) else t2.length - t1.length })
        rountmap2 = rountmap2.toSortedMap(kotlin.Comparator { t1, t2 -> if (t1.length == t2.length) t1.compareTo(t2) else t2.length - t1.length })

    }

    override fun serve(session: IHTTPSession?): Response {
        val resource = resourceManager.getResource(session!!.uri)
        return resource.serve(session)
    }

    fun registerResource(rs: Resource): Boolean {
        return resourceManager.registerResource(rs)
    }

    private val resourceManager: ResourceManager = ResourceManager()

    private class ResourceManager {
        private var resources = sortedMapOf<String, Resource>()
        fun registerResource(rs: Resource): Boolean {
            val matchers = rs.matchs
            if (!isleager(matchers))
                return false
            matchers.forEach {
                resources.put(it, rs)
            }
            resources = resources.toSortedMap(Comparator { t1, t2 ->
                var tmp = 0
                tmp += if (t1.endsWith("/[\\d\\D]*")) 0 else 1
                tmp += if (t2.endsWith("/[\\d\\D]*")) 0 else 2
                when (tmp) {
                    0, 3 -> when (t1.length == t2.length) {
                        true -> t1.compareTo(t2)
                        false -> t2.length - t1.length
                    }
                    1 -> -1
                    2 -> 1
                    else -> 0
                }
            })
            for ((k, v) in resources) {
                logi(k)
            }
            return true
        }

        fun getResource(uri: String): Resource {
            for ((key, resource) in resources) {
                if (uri.matches(key.toRegex()))
                    return resource
            }
            return DefaultResource()
        }

        fun isleager(matchers: Array<String>): Boolean {
            return true
        }
    }

    private class HKAsyncRunner : AsyncRunner {
        private val threads = Executors.newCachedThreadPool()
        private var requestCount: Long = 0

        override fun closeAll() {
            threads.shutdownNow()
        }

        override fun closed(clientHandler: ClientHandler?) {
            --this.requestCount
        }

        override fun exec(code: ClientHandler?) {
            ++this.requestCount
            threads.execute(code)
        }
    }

    class HKResponse(status: IStatus = Status.OK, mimeType: String = NanoHTTPD.MIME_HTML, data: InputStream? = null, totalBytes: Long = 0) : Response(status, mimeType, data, totalBytes) {
        val gmtFrmt = SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US)
        var totol = totalBytes
        val headers = HashMap<String, String>()

        override fun addHeader(name: String, value: String) {
            headers[name] = value//if (header.containsKey(name))
//            super.addHeader(name, value)
        }

        fun setContentLengthLong(length: Long) {
            addHeader("Content-Length", length.toString())
        }

        fun setDateHeader(header: String, time: Long) {
            addHeader(header, gmtFrmt.format(Date(time)))
        }

        fun sendErro(status: IStatus, info: String = "") {
            this.status = status
            val input = ByteArrayInputStream(info.toByteArray())
            addHeader("Content-Type", "text/plain")
            setContentLengthLong(info.length.toLong())
            this.data = input
        }

        fun sendHtml(status: IStatus, info: String = "") {
            this.status = status
            val input = ByteArrayInputStream(info.toByteArray())
            addHeader("Content-Type", "text/html")
            setContentLengthLong(info.length.toLong())
            this.data = input
        }

        override fun setStatus(status: IStatus?) {
            super.setStatus(status)
        }

        override fun send(outputStream: OutputStream?) {
            try {
                if (this.status == null) {
                    throw Error("sendResponse(): Status can't be null.")
                }
                val pw = PrintWriter(BufferedWriter(OutputStreamWriter(outputStream, ContentType(this.mimeType).encoding)), false)
                pw.append("HTTP/1.1 ").append(this.status.description).append(" \r\n")
                if (getHeader("date") == null) {
                    printHeader(pw, "Date", gmtFrmt.format(Date()))
                }
                for ((k, v) in headers)
                    printHeader(pw, k, v)
                pw.append("\r\n")
                pw.flush()
                outputStream?.let {
                    val length = headers["Content-Length"]?.toLong() ?: 0
                    sendBody(it, length)
                }
                this.data.close()
            } catch (ioe: IOException) {
            }
        }

        private fun sendBody(outputStream: OutputStream, pending: Long) {
            val BUFFER_SIZE = 16 * 1024
            val buff = ByteArray(BUFFER_SIZE)
            val sendEverything = pending == -1L
            var len = pending.toInt()
            val bufferRead = BufferedInputStream(data)
            val bufferWriter = BufferedOutputStream(outputStream)
            while (len > 0 || sendEverything) {
                val bytesToRead = if (sendEverything) BUFFER_SIZE else Math.min(len, BUFFER_SIZE)
                val read = bufferRead.read(buff, 0, bytesToRead)
                if (read <= 0) {
                    break
                }
                bufferWriter.write(buff, 0, read)
                if (!sendEverything) {
                    len -= read
                }
            }
            bufferWriter.flush()
            bufferRead.close()
            bufferWriter.close()
        }

    }
}
