package io.github.lumine1909.nopacketban.handler;

import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationTargetException;

public interface SecurityChecker<T> {

    Throwable checkSecurity(ChannelHandlerContext ctx, T msg) throws Throwable;

    static Throwable unwrap(Throwable t) {
        while (t != null) {
            if (t instanceof InvocationTargetException e) {
                t = e.getTargetException();
            } else if (t instanceof RuntimeException e && e.getCause() != null) {
                t = e.getCause();
            } else {
                break;
            }
        }
        return t;
    }
}
