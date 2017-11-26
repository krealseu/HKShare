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
import org.kreal.hkshare.extensions.logi
import java.net.InetSocketAddress


/**
 * Created by lthee on 2017/10/21.
 */
class HttpService(private val port: Int) : Runnable {
    var channel: Channel? = null

    fun stop() = channel?.close()

    fun start() = if (!isAlive()) Thread(this).start() else false

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
            channel?.apply {
                closeFuture().sync() // wait until the serviceChannel been bound
            }
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

}