package io.zensoft.web

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.zensoft.web.handler.HttpControllerHandler
import org.springframework.stereotype.Component

@Component
class HttpChannelInitializer(
        val httpControllerHandler: HttpControllerHandler
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(HttpRequestDecoder())
        pipeline.addLast(HttpObjectAggregator(20971520)) //1048576
        pipeline.addLast(HttpResponseEncoder())
        pipeline.addLast(httpControllerHandler)
    }

}