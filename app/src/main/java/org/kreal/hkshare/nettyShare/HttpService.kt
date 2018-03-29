package org.kreal.hkshare.nettyShare

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.stream.ChunkedWriteHandler
import java.net.InetSocketAddress


/**
 * Created by lthee on 2017/10/21.
 * 文件共享的netty服务
 */
class HttpService(private val port: Int) : Runnable {
    var channel: Channel? = null

    fun stop() = channel?.close()

    fun start() = if (!isAlive()) Thread(this).start() else Unit

    fun isAlive(): Boolean = channel?.isOpen ?: false

    override fun run() {
        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()
        try {
            val b = ServerBootstrap()
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel?) {
                            ch?.apply {
                                pipeline().addLast("http-codec", HttpServerCodec())
//                                pipeline().addLast(HttpContentCompressor())
                                pipeline().addLast("aggregator", HttpObjectAggregator(512 * 1024))
                                pipeline().addLast("http-chunked", ChunkedWriteHandler())
//                                pipeline().addLast(IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))
//                                pipeline().addLast(HeartbeatHandler())
                                pipeline().addLast(FileShareHandler())
                            }
                        }
                    })
            channel = b.bind(InetSocketAddress(port)).sync().channel()         // wait until the port been bound
            channel?.closeFuture()?.sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

}