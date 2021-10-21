package com.geekbrains.server;

import com.geekbrains.common.AbstractMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        log.debug("received: {}", msg);
        ctx.writeAndFlush(msg);
    }
}