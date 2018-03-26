package io.zensoft.web

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.zensoft.web.handler.HttpControllerHandler
import org.springframework.stereotype.Component

@Component
class HttpChannelInitializer(
        val httpControllerHandler: HttpControllerHandler
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(HttpServerCodec())
        pipeline.addLast(HttpObjectAggregator(20971520)) //1048576
        pipeline.addLast(httpControllerHandler)
    }

}