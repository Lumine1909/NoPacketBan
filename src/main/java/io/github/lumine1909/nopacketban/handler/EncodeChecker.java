package io.github.lumine1909.nopacketban.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;

import static io.github.lumine1909.nopacketban.util.Reflection.encoderProtocolInfo;
import static io.github.lumine1909.nopacketban.util.Reflection.messageToByteEncode;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class EncodeChecker<T extends PacketListener> extends ChannelOutboundHandlerAdapter implements SecurityChecker<Packet> {

    private final Player player;
    private final PacketEncoder<T> dummyEncoder;

    public EncodeChecker(Player player, PacketEncoder<T> encoder) {
        this.dummyEncoder = new PacketEncoder<>(encoderProtocolInfo.get(encoder));
        this.player = player;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet)) {
            super.write(ctx, msg, promise);
            return;
        }
        try {
            checkSecurity(ctx, (Packet) msg);
            super.write(ctx, msg, promise);
        } catch (Throwable t) {
            player.sendMessage(join(JoinConfiguration.newlines(),
                text("Failed to encode packet " + msg.getClass().getSimpleName() + " .", NamedTextColor.RED),
                text("Please report to server admin if you believe this is in error.", NamedTextColor.RED),
                text("Or consider you are being packet kicked.", NamedTextColor.RED),
                text("Details: " + t.getMessage(), NamedTextColor.RED)
            ));
        }
    }

    public void checkSecurity(ChannelHandlerContext ctx, Packet msg) throws Exception {
        messageToByteEncode.invoke(dummyEncoder, ctx, msg, ctx.alloc().ioBuffer());
    }
}
