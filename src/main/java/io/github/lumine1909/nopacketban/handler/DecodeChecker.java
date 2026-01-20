package io.github.lumine1909.nopacketban.handler;

import io.github.lumine1909.nopacketban.util.DummyList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketListener;
import org.bukkit.entity.Player;

import static io.github.lumine1909.nopacketban.util.Reflection.byteToMessageDecode;
import static io.github.lumine1909.nopacketban.util.Reflection.decoderProtocolInfo;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class DecodeChecker<T extends PacketListener> extends ChannelInboundHandlerAdapter implements SecurityChecker<ByteBuf> {

    private final Player player;
    private final PacketDecoder<T> dummyDecoder;

    public DecodeChecker(Player player, PacketDecoder<T> decoder) {
        this.dummyDecoder = new PacketDecoder<>(decoderProtocolInfo.getUntyped(decoder));
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf buf)) {
            super.channelRead(ctx, msg);
            return;
        }
        Throwable t = checkSecurity(ctx, buf);
        if (t == null) {
            super.channelRead(ctx, buf);
            return;
        }
        buf.release();
        player.sendMessage(join(JoinConfiguration.newlines(),
            text("Failed to decode message " + buf.getClass().getSimpleName() + " .", NamedTextColor.RED),
            text("Please report to server admin if you believe this is in error.", NamedTextColor.RED),
            text("Or consider you are sending corrupted packet.", NamedTextColor.RED),
            text("Details: " + t.getMessage(), NamedTextColor.RED)
        ));
    }

    public Throwable checkSecurity(ChannelHandlerContext ctx, ByteBuf buf) {
        int reader = buf.readerIndex(), writer = buf.writerIndex();
        try {
            byteToMessageDecode.invoke(dummyDecoder, ctx, buf, DummyList.INSTANCE);
        } catch (Throwable t) {
            return SecurityChecker.unwrap(t);
        } finally {
            buf.readerIndex(reader);
            buf.writerIndex(writer);
        }
        return null;
    }
}