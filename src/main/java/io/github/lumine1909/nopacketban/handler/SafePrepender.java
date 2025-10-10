package io.github.lumine1909.nopacketban.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.Varint21LengthFieldPrepender;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class SafePrepender extends Varint21LengthFieldPrepender {

    private final Player player;

    public SafePrepender(Player player, Varint21LengthFieldPrepender prepender) {
        this.player = player;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        try {
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
}
