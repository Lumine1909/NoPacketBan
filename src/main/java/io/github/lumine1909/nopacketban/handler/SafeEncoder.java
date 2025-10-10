package io.github.lumine1909.nopacketban.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import org.bukkit.entity.Player;

import static io.github.lumine1909.nopacketban.util.Reflection.encoderProtocolInfo;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class SafeEncoder<T extends PacketListener> extends PacketEncoder<T> {

    private final Player player;

    public SafeEncoder(Player player, PacketEncoder<T> encoder) {
        super(encoderProtocolInfo.get(encoder));
        this.player = player;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        try {
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
}
