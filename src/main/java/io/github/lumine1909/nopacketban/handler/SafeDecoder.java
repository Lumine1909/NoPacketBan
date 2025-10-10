package io.github.lumine1909.nopacketban.handler;

import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketListener;
import org.bukkit.entity.Player;

import static io.github.lumine1909.nopacketban.util.Reflection.decoderProtocolInfo;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class SafeDecoder<T extends PacketListener> extends PacketDecoder<T> {

    private final Player player;

    public SafeDecoder(Player player, PacketDecoder<T> decoder) {
        super(decoderProtocolInfo.get(decoder));
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            super.channelRead(ctx, msg);
        } catch (Throwable t) {
            player.sendMessage(join(JoinConfiguration.newlines(),
                text("Failed to decode message " + msg.getClass().getSimpleName() + " .", NamedTextColor.RED),
                text("Please report to server admin if you believe this is in error.", NamedTextColor.RED),
                text("Or consider you are sending corrupted packet.", NamedTextColor.RED),
                text("Details: " + t.getMessage(), NamedTextColor.RED)
            ));
        }
    }
}
