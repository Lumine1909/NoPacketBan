package io.github.lumine1909.nopacketban.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketListener;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static io.github.lumine1909.nopacketban.util.Reflection.byteToMessageDecode;
import static io.github.lumine1909.nopacketban.util.Reflection.decoderProtocolInfo;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class DecodeChecker<T extends PacketListener> extends ChannelInboundHandlerAdapter implements SecurityChecker<ByteBuf> {

    private final Player player;
    private final PacketDecoder<T> dummyDecoder;

    public DecodeChecker(Player player, PacketDecoder<T> decoder) {
        this.dummyDecoder = new PacketDecoder<>(decoderProtocolInfo.get(decoder));
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            super.channelRead(ctx, msg);
            return;
        }
        try {
            checkSecurity(ctx, (ByteBuf) msg);
            ctx.fireChannelRead(msg);
        } catch (Throwable t) {
            player.sendMessage(join(JoinConfiguration.newlines(),
                text("Failed to decode message " + msg.getClass().getSimpleName() + " .", NamedTextColor.RED),
                text("Please report to server admin if you believe this is in error.", NamedTextColor.RED),
                text("Or consider you are sending corrupted packet.", NamedTextColor.RED),
                text("Details: " + t.getMessage(), NamedTextColor.RED)
            ));
        }
    }

    public void checkSecurity(ChannelHandlerContext ctx, ByteBuf msg) throws Throwable {
        ByteBuf buf = msg.retainedDuplicate();
        try {
            byteToMessageDecode.invoke(dummyDecoder, ctx, buf, new ArrayList<>());
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            buf.release();
        }
    }
}