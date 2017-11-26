package org.kreal.hkshare.extensions

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpResponseStatus.FOUND
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpResponse


/**
 * Created by lthee on 2017/10/28.
 */
fun ChannelHandlerContext.sendError(state: HttpResponseStatus, info: String) {
    val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, state, Unpooled.wrappedBuffer(info.toByteArray()))
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
    this.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
}

fun ChannelHandlerContext.sendError(code: Int, info: String) {
    sendError(HttpResponseStatus.valueOf(code), info)
}

fun ChannelHandlerContext.sendRedirect(newUri: String) {
    val response = DefaultFullHttpResponse(HTTP_1_1, FOUND)
    response.headers().set(HttpHeaderNames.LOCATION, newUri)
    writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
}