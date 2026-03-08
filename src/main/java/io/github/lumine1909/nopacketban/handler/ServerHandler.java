package io.github.lumine1909.nopacketban.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import static io.github.lumine1909.nopacketban.NoPacketBan.LOG_PACKET_EXCEPTIONS;
import static io.github.lumine1909.nopacketban.NoPacketBan.plugin;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public class ServerHandler extends ChannelDuplexHandler {

    private final Channel channel;

    public ServerHandler(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable exception) throws Exception {
        if (exception instanceof io.netty.handler.codec.EncoderException && exception.getCause() instanceof PacketEncoder.PacketTooLargeException packetTooLargeException) {
            final Packet<?> packet = packetTooLargeException.getPacket();
            if (packet.hasLargePacketFallback()) {
                super.exceptionCaught(ctx, exception);
                return;
            }
        }
        if (LOG_PACKET_EXCEPTIONS) {
            plugin.getSLF4JLogger().warn("Packet exception occurred: ", exception);
        }
        Connection connection = (Connection) ctx.channel().pipeline().get("packet_handler");
        if (connection == null || connection.getPacketListener() == null || connection.getPacketListener().protocol() != ConnectionProtocol.PLAY) {
            ctx.fireExceptionCaught(exception);
            return;
        }
        channel.writeAndFlush(new ClientboundSystemChatPacket(join(JoinConfiguration.newlines(),
            text("An error occurred on sending packet.", NamedTextColor.RED),
            text("Please report to server admin if you believe this is in error.", NamedTextColor.RED),
            text("Or consider you are being packet kicked.", NamedTextColor.RED),
            text("Details: " + exception.getMessage(), NamedTextColor.RED)
        ), false));
    }
}
