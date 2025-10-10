package io.github.lumine1909.nopacketban.handler;

import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.Varint21FrameDecoder;
import org.bukkit.entity.Player;

import static io.github.lumine1909.nopacketban.util.Reflection.monitor;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class SafeSplitter extends Varint21FrameDecoder {

    private final Player player;

    public SafeSplitter(Player player, Varint21FrameDecoder splitter) {
        super(monitor.get(splitter));
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
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
}
