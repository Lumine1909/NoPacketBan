package io.github.lumine1909.nopacketban.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;

import static io.github.lumine1909.nopacketban.NoPacketBan.*;
import static io.github.lumine1909.nopacketban.util.Reflection.encoderProtocolInfo;
import static io.github.lumine1909.nopacketban.util.Reflection.messageToByteEncode;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

@SuppressWarnings("rawtypes")
public class EncodeChecker<T extends PacketListener> extends ChannelOutboundHandlerAdapter implements SecurityChecker<Packet> {

    private final Player player;
    private final PacketEncoder<T> dummyEncoder;

    public EncodeChecker(Player player, PacketEncoder<T> encoder) {
        this.dummyEncoder = new PacketEncoder<>(encoderProtocolInfo.getUntyped(encoder));
        this.player = player;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet packet)) {
            super.write(ctx, msg, promise);
            return;
        }
        Throwable t = checkSecurity(ctx, packet);
        if (t == null || packet.hasLargePacketFallback()) {
            super.write(ctx, packet, promise);
            return;
        }
        player.sendMessage(join(JoinConfiguration.newlines(),
            text("Failed to encode packet " + packet.getClass().getSimpleName() + " .", NamedTextColor.RED),
            text("Please report to server admin if you believe this is in error.", NamedTextColor.RED),
            text("Or consider you are being packet kicked.", NamedTextColor.RED),
            text("Details: " + t.getMessage(), NamedTextColor.RED)
        ));
    }

    public Throwable checkSecurity(ChannelHandlerContext ctx, Packet packet) {
        ByteBuf buf = ctx.alloc().heapBuffer();
        try {
            messageToByteEncode.invoke(dummyEncoder, ctx, packet, buf);
            if (!hasVia) {
                return null;
            }
            int packetLength = buf.readableBytes();
            if (packetLength > (MAX_PACKET_SIZE)) {
                return new StacklessPacketOversizeException(packet, packetLength, MAX_PACKET_SIZE);
            }
            if (packetLength > MAX_NORMAL_PACKET_SIZE && !packet.hasLargePacketFallback()) {
                return new StacklessPacketOversizeException(packet, packetLength, MAX_NORMAL_PACKET_SIZE);
            }
        } catch (Throwable t) {
            return SecurityChecker.unwrap(t);
        } finally {
            buf.release();
        }
        return null;
    }

    private static class StacklessPacketOversizeException extends RuntimeException {

        private static final StackTraceElement[] DUMMY_STACK_TRACE = new StackTraceElement[0];

        private static final String NOTICE = """
            PacketTooLarge - %s is %s. Max is %s.
            This is not the vanilla limit, but because ViaVersion is installed on the server, the server sets %s as a reserve size to prevent client-side DecoderExceptions caused by unexpected size inflation.
            If you believe the preserved size is too large, please report to the server admin.
            """;

        StacklessPacketOversizeException(Packet<?> packet, int packetLength, int maxLength) {
            super(NOTICE.formatted(packet.getClass().getSimpleName(), packetLength, maxLength, RESERVED_PACKET_SIZE));
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
