package io.github.lumine1909.nopacketban.handler;

import io.github.lumine1909.nopacketban.util.DummyList;
import io.github.lumine1909.nopacketban.util.DummyMonitor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.Varint21FrameDecoder;
import org.bukkit.entity.Player;

import static io.github.lumine1909.nopacketban.util.Reflection.byteToMessageDecode;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class SplitChecker extends ChannelInboundHandlerAdapter implements SecurityChecker<ByteBuf> {

    private final Player player;
    private final Varint21FrameDecoder dummySplitter;

    public SplitChecker(Player player) {
        this.player = player;
        this.dummySplitter = new Varint21FrameDecoder(DummyMonitor.INSTANCE);
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
            text("Failed to split message " + msg.getClass().getSimpleName() + " .", NamedTextColor.RED),
            text("Please report to server admin if you believe this is in error.", NamedTextColor.RED),
            text("Or consider you are sending a huge packet.", NamedTextColor.RED),
            text("Details: " + t.getMessage(), NamedTextColor.RED)
        ));
    }

    public Throwable checkSecurity(ChannelHandlerContext ctx, ByteBuf msg) {
        int reader = msg.readerIndex(), writer = msg.writerIndex();
        try {
            byteToMessageDecode.invokeFast(dummySplitter, ctx, msg, DummyList.INSTANCE);
        } catch (RuntimeException e) {
            return e.getCause();
        } catch (Throwable t) {
            return t;
        } finally {
            msg.readerIndex(reader);
            msg.writerIndex(writer);
        }
        return null;
    }
}
