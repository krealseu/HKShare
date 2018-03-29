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
import org.kreal.hkshare.nettyShare.httpFile.FileSystem
import org.kreal.hkshare.nettyShare.httpFile.HttpFile
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

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
        val path: String = sanitizeUri(uri)
                ?: return ctx.sendError(HttpResponseStatus.FORBIDDEN, "FORBIDDEN")
        when {
            path.indexOf('?') >= 0 -> return ctx.sendError(HttpResponseStatus.FORBIDDEN, "FORBIDDEN: Won't server ? for security reasons.")
            path.contains("../") -> return ctx.sendError(HttpResponseStatus.FORBIDDEN, "FORBIDDEN: Won't server ../ for security reasons.")
        }

        val file = FileSystem.instance.newHttpFile(path)
        when {
            !file.exist() -> return ctx.sendError(404, "Error 404,resource not fount ..")
            file.isDirectory -> return sendDirectory(ctx, request, file)
            file.isFile -> sendFile(ctx, request, file)
        }
    }


    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        ctx?.close()
    }

    companion object {
        private val INSECURE_URI = Pattern.compile(".*[<>&\"].*")
        private fun sanitizeUri(uri: String): String? {
            val uriPath: String
            try {
                uriPath = URLDecoder.decode(uri, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                throw Error(e)
            }
            if (uriPath.isEmpty())
                return null
            val filePath = uriPath.replace('/', File.separatorChar)
            if (filePath.contains("..${File.separator}") || filePath[0] != '/')
                return null
            return filePath
        }

        private fun sendFile(ctx: ChannelHandlerContext, request: FullHttpRequest, file: HttpFile) {
            val gmtFrmt = SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US)
            val response = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
//            if (!file.isFile) {
//                response.setStatus(HttpResponseStatus.NOT_FOUND)
//                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
//                return
//            }
            response.headers().set(HttpHeaderNames.DATE, gmtFrmt.format(Date()))
//        response.headers().set(HttpHeaderNames.AGE, 600.toString())
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length().toString())
            response.headers().set(HttpHeaderNames.LAST_MODIFIED, gmtFrmt.format(Date(file.lastModified())))
            response.headers().set(HttpHeaderNames.ACCEPT_RANGES, "bytes")
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, file.getMimeType())
            response.headers()[HttpHeaderNames.ETAG] = file.eTag
            response.headers()[HttpHeaderNames.CONNECTION] = request.headers()[HttpHeaderNames.CONNECTION]


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
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, html.readableBytes().toString())
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
            response.headers().set(HttpHeaderNames.ETAG, file.eTag)
            response.headers()[HttpHeaderNames.CONNECTION] = request.headers()[HttpHeaderNames.CONNECTION]
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
            """
        }

        private fun listDirectory(file: HttpFile): String {
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
            for (f in file.listFiles()) {
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
    }
}