package org.kreal.hkshare.nettyShare

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.DefaultFileRegion
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedStream
import org.kreal.hkshare.extensions.getAppico
import org.kreal.hkshare.extensions.sendError
import org.kreal.hkshare.extensions.sendRedirect
import org.kreal.hkshare.nettyShare.httpFile.HttpFileSystem
import org.kreal.hkshare.nettyShare.httpFile.HttpFile
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lthee on 2017/10/28.
 * 处理文件共享，netty的Handler
 */
class FileShareHandler : SimpleChannelInboundHandler<FullHttpRequest>() {

    override fun messageReceived(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        when {
            !request.decoderResult().isSuccess -> return ctx.sendError(HttpResponseStatus.BAD_REQUEST, "BAD_REQUEST")
            request.method() != HttpMethod.GET -> return ctx.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED")
        }
        val uri = request.uri()
        val path: String = try {
            URLDecoder.decode(uri, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            return ctx.sendError(HttpResponseStatus.FORBIDDEN, "FORBIDDEN: $e")
        }

        when {
            path.indexOf('?') >= 0 -> return ctx.sendError(HttpResponseStatus.FORBIDDEN, "FORBIDDEN: Won't server ? for security reasons.")
            path.contains("../") -> return ctx.sendError(HttpResponseStatus.FORBIDDEN, "FORBIDDEN: Won't server ../ for security reasons.")
            path.contains(illegalChar) -> return ctx.sendError(HttpResponseStatus.FORBIDDEN, "FORBIDDEN: Uri path contain illegal char ")
        }

        val file = HttpFileSystem.instance.newHttpFile(path)
        when {
            !file.exist() -> return ctx.sendError(404, "Error 404,resource not fount ..")
            file.isDirectory ->
                if (path.endsWith('/')) return sendDirectory(ctx, request, file) else ctx.sendRedirect("$uri/")
            file.isFile -> sendFile(ctx, request, file)
        }
    }


    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        ctx?.close()
    }

    companion object {
        private val illegalChar = "[*<>|?\"\\\\]".toRegex()
        private val gmtFormat = SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.SIMPLIFIED_CHINESE)

        private fun sendFile(ctx: ChannelHandlerContext, request: FullHttpRequest, file: HttpFile) {
            val response = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
            response.headers().set(HttpHeaderNames.DATE, gmtFormat.format(Date()))
//                    .set(HttpHeaderNames.AGE, 600.toString())
                    .set(HttpHeaderNames.CONTENT_LENGTH, file.length().toString())
                    .set(HttpHeaderNames.LAST_MODIFIED, gmtFormat.format(Date(file.lastModified())))
                    .set(HttpHeaderNames.ACCEPT_RANGES, "bytes")
                    .set(HttpHeaderNames.CONTENT_TYPE, file.getMimeType())
                    .set(HttpHeaderNames.ETAG, file.eTag)
                    .set(HttpHeaderNames.CONNECTION, request.headers()[HttpHeaderNames.CONNECTION])


            val ifNoneMatch = request.headers().get(HttpHeaderNames.IF_NONE_MATCH)
            if (ifNoneMatch != null) {
                if (ifNoneMatch == file.eTag) {
                    response.setStatus(HttpResponseStatus.NOT_MODIFIED)
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
                    return
                }
            }

            // detail range
            val range = request.headers().get(HttpHeaderNames.RANGE)
            val fSize = file.length()
            val pos = LongArray(2)
            pos[0] = 0
            pos[1] = fSize - 1L
            if (range != null) {
                var isstandard = true
                //初始化Range的个参数，并判断是否正规
                if (range.matches("bytes=\\d*-\\d*$".toRegex())) {
                    val tmp = range.replace("bytes=".toRegex(), "")
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
                    response.setStatus(HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    ctx.writeAndFlush(response)?.addListener(ChannelFutureListener.CLOSE)
                    return
                }
                //deal with If Match
                val ifMatch = request.headers().get(HttpHeaderNames.IF_MATCH)
                if (ifMatch != null) {
                    if (ifMatch != file.eTag) {
                        response.setStatus(HttpResponseStatus.PRECONDITION_FAILED)
                        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
                    }
                }
                //detail with if Range
                val ifRange = request.headers().get(HttpHeaderNames.IF_RANGE)
                if (ifRange != null) {
                    if (ifRange != file.eTag) {
                        pos[0] = 0
                        pos[1] = fSize - 1L
                    }
                }
                response.setStatus(HttpResponseStatus.PARTIAL_CONTENT)
                val contentRange = String.format("bytes %d-%d/%d", pos[0], pos[1], fSize)
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, (pos[1] - pos[0] + 1L).toString())
                response.headers().set(HttpHeaderNames.CONTENT_RANGE, contentRange)
            } else {
                response.setStatus(HttpResponseStatus.OK)
            }
//        request.release()
            ctx.write(response)
            if (file.channel == null)
                ctx.write(ChunkedStream(file.getInputStream(pos[0])))
            else
                ctx.write(DefaultFileRegion(file.channel, pos[0], pos[1] - pos[0] + 1L))
            // Write the end marker
            val future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
            if (request.headers().get(HttpHeaderNames.CONNECTION) != "keep-alive")
                future.addListener(ChannelFutureListener.CLOSE)
        }

        private fun sendDirectory(ctx: ChannelHandlerContext, request: FullHttpRequest, file: HttpFile) {
            val html = Unpooled.wrappedBuffer(listDirectory(file).toByteArray())
            val gmtFmt = SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US)
            val response = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
            response.headers().set(HttpHeaderNames.DATE, gmtFmt.format(Date()))
                    .set(HttpHeaderNames.CONTENT_LENGTH, html.readableBytes().toString())
                    .set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
                    .set(HttpHeaderNames.ETAG, file.eTag)
                    .set(HttpHeaderNames.CONNECTION, request.headers()[HttpHeaderNames.CONNECTION])
            val ifNoneMatch = request.headers().get(HttpHeaderNames.IF_NONE_MATCH)
            if (ifNoneMatch != null) {
                if (ifNoneMatch == file.eTag) {
                    response.setStatus(HttpResponseStatus.NOT_MODIFIED)
                    html.release()
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
                    return
                }
            }
            ctx.write(response)
            ctx.write(html)
            val future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
            if (request.headers().get(HttpHeaderNames.CONNECTION) != "keep-alive")
                future.addListener(ChannelFutureListener.CLOSE)
        }

        private fun itemDiv(uri: String, image: String, title: String, size: String, date: String): String {
            return """
                <div class="item">
                <img src="$image"/>
                <a href="$uri">
                <div class="title">$title</div>
                <div class="size">$size</div>
                <div class="date">$date</div>
                <div class="line"></div>
                </a>
                </div>
            """.trimIndent()
        }

        private fun listDirectory(file: HttpFile): String {
            val htmlBuilder = StringBuilder()
            htmlBuilder.append("""
                <html><head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <meta name="viewport" content="width=device-width,initial-scale=1"/>
                <link href="/assets/appico/home.png" rel="shortcut icon"/>
                <link href="/assets/css/itemstyle.css" rel="stylesheet" type="text/css"/>
                <title>${file.name}</title>
                </head><body><h1>${file.name}</h1>
                """.trimIndent())
            htmlBuilder.append("<div class=\"cc\">")
            var up: String? = null
            var path = file.uri
            if (path.length > 1) {
                path = path.removeSuffix("/")
                val slash = path.lastIndexOf('/')
                up = when (slash) {
                    -1 -> null
                    0 -> "/"
                    else -> path.substring(0, slash)
                }
            }
            if (up != null) {
                htmlBuilder.append(itemDiv("$up", "/assets/appico/back.png", "..", "back", ""))
            }
            file.listFiles().forEach {
                when {
                    it.isDirectory -> htmlBuilder.append(itemDiv(it.uri, "/assets/appico/folder.png", it.name, if (it.listFiles().isEmpty()) "Empty" else "${it.listFiles().size} files ", gmtFormat.format(Date(it.lastModified()))))
                    it.isFile -> htmlBuilder.append(itemDiv(it.uri, it.name.getAppico(), it.name, length2String(it.length()), gmtFormat.format(Date(it.lastModified()))))
                }
            }
            htmlBuilder.append("""
                </div>
                </body></html>
                """.trimIndent())
            return htmlBuilder.toString()
        }

        private fun length2String(len: Long): String = when (len) {
            in 0..1024 -> len.toString() + " bytes"
            in 1024..1024 * 1024 -> (len / 1024).toString() + "." + (len % 1024 / 10 % 100) + " KB"
            else -> (len / (1024 * 1024)).toString() + "." + (len % (1024 * 1024) / 10 % 100) + " MB"
        }
    }
}