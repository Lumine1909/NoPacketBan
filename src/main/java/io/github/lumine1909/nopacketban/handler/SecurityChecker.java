package io.github.lumine1909.nopacketban.handler;

import io.netty.channel.ChannelHandlerContext;

public interface SecurityChecker<T> {

    void checkSecurity(ChannelHandlerContext ctx, T msg) throws Exception;
}
