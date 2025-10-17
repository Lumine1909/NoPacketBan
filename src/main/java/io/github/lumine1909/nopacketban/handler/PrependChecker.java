package io.github.lumine1909.nopacketban.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.Varint21LengthFieldPrepender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

import static io.github.lumine1909.nopacketban.util.Reflection.messageToByteEncode;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class PrependChecker extends ChannelOutboundHandlerAdapter implements SecurityChecker<ByteBuf> {

    private final Player player;
    private final Varint21LengthFieldPrepender dummyPrepender;

    public PrependChecker(Player player) {
        this.player = player;
        this.dummyPrepender = new Varint21LengthFieldPrepender();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            super.write(ctx, msg, promise);
            return;
        }
        try {
            checkSecurity(ctx, (ByteBuf) msg);
            super.write(ctx, msg, promise);
        } catch (Throwable t) {
            player.sendMessage(join(JoinConfiguration.newlines(),
                text("Failed to prepend " + msg.getClass().getSimpleName() + " .", NamedTextColor.RED),
                text("Please report to server admin if you believe this is in error.", NamedTextColor.RED),
                text("Or consider you are being packet kicked.", NamedTextColor.RED),
                text("Details: " + t.getMessage(), NamedTextColor.RED)
            ));
        }
    }

    @Override
    public void checkSecurity(ChannelHandlerContext ctx, ByteBuf msg) throws Throwable {
        try {
            messageToByteEncode.invoke(dummyPrepender, ctx, msg.retainedDuplicate(), ctx.alloc().ioBuffer());
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
