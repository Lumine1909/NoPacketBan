package io.github.lumine1909.nopacketban;

import io.github.lumine1909.nopacketban.handler.ClientHandler;
import io.github.lumine1909.nopacketban.handler.ServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;


public class Injector {

    public static void injectPlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        inject(channel);
    }

    public static void inject(Channel channel) {
        channel.pipeline().addBefore("packet_handler", "npb_server_handler", new ServerHandler(channel));
        channel.pipeline().addAfter("decoder", "npb_client_handler", ClientHandler.INSTANCE);
    }

    public static void uninjectPlayer(Player player) {
        if (player == null) {
            return;
        }
        uninject(((CraftPlayer) player).getHandle().connection.connection.channel);
    }

    public static void uninject(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        try {
            pipeline.remove("npb_server_handler");
            pipeline.remove("npb_client_handler");
        } catch (Exception ignored) {
        }
    }
}