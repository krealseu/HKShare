package org.kreal.hkshare.nettyShare

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.IdleStateEvent
import io.netty.util.CharsetUtil

/**
 * Created by lthee on 2017/10/22.
 */
class HeartbeatHandler : ChannelInboundHandlerAdapter() {
    private val HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("HEARTBEAT", CharsetUtil.UTF_8))
    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        if (evt is IdleStateEvent) {
            ctx?.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate())?.addListener(ChannelFutureListener.CLOSE)
        } else
            super.userEventTriggered(ctx, evt)
    }
}