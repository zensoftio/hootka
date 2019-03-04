package io.zensoft.hootka.api.internal.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.WriteBufferWaterMark
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollChannelOption
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import java.util.*
import javax.annotation.PreDestroy

class HttpServer(
    port: Int,
    private val httpChannelInitializer: HttpChannelInitializer
) : CommandLineRunner {

    companion object {
        private val log = LoggerFactory.getLogger(HttpServer::class.java)
    }

    private var bossGroup: EventLoopGroup
    private var workerGroup: EventLoopGroup
    private var socketChannelClass: Class<out ServerChannel>
    private val port: Int

    init {
        if (port == -1) {
            this.port = Random().nextInt(65_535)
        } else {
            this.port = port
        }
        if (Epoll.isAvailable()) {
            bossGroup = EpollEventLoopGroup(1)
            workerGroup = EpollEventLoopGroup(Runtime.getRuntime().availableProcessors())
            socketChannelClass = EpollServerSocketChannel::class.java
        } else {
            bossGroup = NioEventLoopGroup(1)
            workerGroup = NioEventLoopGroup(Runtime.getRuntime().availableProcessors())
            socketChannelClass = NioServerSocketChannel::class.java
        }
    }


    override fun run(vararg args: String?) {
        try {
            val sb = ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(socketChannelClass)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(httpChannelInitializer)
                .option(ChannelOption.SO_BACKLOG, 512)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)

            if (Epoll.isAvailable()) {
                sb.option(EpollChannelOption.SO_REUSEPORT, true)
            }

            val future = sb.bind(port)
            log.info("Server is started on {} port.", port)

            future.sync() // locking the thread until groups are going on
            future.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            log.error("Something went wrong", e)
        } finally {
            shutdown()
        }
    }

    @PreDestroy
    private fun shutdown() {
        log.info("Server is shutting down.")
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }

}
