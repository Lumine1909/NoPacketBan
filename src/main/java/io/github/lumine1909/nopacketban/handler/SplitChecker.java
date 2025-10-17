package io.github.lumine1909.nopacketban.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Varint21FrameDecoder;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static io.github.lumine1909.nopacketban.util.Reflection.byteToMessageDecode;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class SplitChecker extends ChannelInboundHandlerAdapter implements SecurityChecker<ByteBuf> {

    static class DummyMonitor extends BandwidthDebugMonitor {

        @SuppressWarnings("DataFlowIssue")
        public DummyMonitor() {
            super(null);
        }

        @Override
        public void tick() {
        }

        @Override
        public void onReceive(int amount) {
        }
    }

    private final Player player;
    private final Varint21FrameDecoder dummySplitter;

    public SplitChecker(Player player) {
        this.player = player;
        this.dummySplitter = new Varint21FrameDecoder(new DummyMonitor());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            super.channelRead(ctx, msg);
            return;
        }
        try {
            checkSecurity(ctx, (ByteBuf) msg);
            super.channelRead(ctx, msg);
        } catch (Throwable t) {
            player.sendMessage(join(JoinConfiguration.newlines(),
                text("Failed to split message " + msg.getClass().getSimpleName() + " .", NamedTextColor.RED),
                text("Please report to server admin if you believe this is in error.", NamedTextColor.RED),
                text("Or consider you are sending a huge packet.", NamedTextColor.RED),
                text("Details: " + t.getMessage(), NamedTextColor.RED)
            ));
        }
    }

    public void checkSecurity(ChannelHandlerContext ctx, ByteBuf msg) throws Throwable {
        ByteBuf buf = msg.retainedDuplicate();
        try {
            byteToMessageDecode.invoke(dummySplitter, ctx, buf, new ArrayList<>());
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            buf.release();
        }
    }
}
