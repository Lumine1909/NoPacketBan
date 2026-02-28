package io.github.lumine1909.nopacketban.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import static io.github.lumine1909.nopacketban.NoPacketBan.MAX_PACKET_SIZE;
import static io.github.lumine1909.nopacketban.NoPacketBan.RESERVED_PACKET_SIZE;

public class ClientHandler extends ChannelDuplexHandler {

    public static final ClientHandler INSTANCE = new ClientHandler();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof ByteBuf buf)) {
            super.write(ctx, msg, promise);
            return;
        }
        int packetLength = buf.readableBytes();
        if (packetLength > MAX_PACKET_SIZE) {
            throw new StacklessOversizeException(packetLength);
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public boolean isSharable() {
        return true;
    }

    private static class StacklessOversizeException extends RuntimeException {

        private static final StackTraceElement[] DUMMY_STACK_TRACE = new StackTraceElement[0];

        private static final String NOTICE = """
            PacketTooLarge - %s is out of the max %s.
            This is not the vanilla limit, but because ViaVersion is installed on the server, the server sets %s as a reserve size to prevent client-side DecoderExceptions caused by unexpected size inflation.
            If you believe the preserved size is too large, please report to the server admin.\s""";

        StacklessOversizeException(int packetLength) {
            super(NOTICE.formatted(packetLength, MAX_PACKET_SIZE, RESERVED_PACKET_SIZE));
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }

        @Override
        public StackTraceElement[] getStackTrace() {
            return DUMMY_STACK_TRACE;
        }
    }
}
